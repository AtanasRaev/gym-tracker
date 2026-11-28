package com.gymtracker.backend.service.impl

import com.gymtracker.backend.config.ApiConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.sql.DataSource

enum class ImportOutcome { SUCCESS, PARTIAL, ALREADY_RUNNING, FAILED }

/**
 * Imports free-exercise-db into the database and uploads its images to Cloudinary.
 * Idempotent: re-running updates existing exercises and only uploads missing images.
 * A PostgreSQL advisory lock keeps concurrent runs (several instances, or the
 * startup seeder and the command) from overlapping.
 */
@Service
class ExerciseImportService(
    private val fetch: FetchDbServiceImpl,
    private val persistence: ExerciseImportPersistence,
    private val uploader: ExerciseImageUploader,
    private val config: ApiConfig,
    private val dataSource: DataSource
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun runImport(): ImportOutcome = try {
        dataSource.connection.use { lock ->
            fun query(sql: String): Boolean = lock.createStatement().use { statement ->
                statement.executeQuery(sql).use { it.next(); it.getBoolean(1) }
            }
            if (!query("SELECT pg_try_advisory_lock($LOCK_KEY)")) {
                logger.info("Exercise import already running elsewhere; skipping")
                return ImportOutcome.ALREADY_RUNNING
            }
            try {
                val failures = importAll { check(lock.isValid(5)) { "Import lock connection lost" } }
                if (failures == 0) ImportOutcome.SUCCESS else ImportOutcome.PARTIAL
            } finally {
                if (!lock.isClosed) query("SELECT pg_advisory_unlock($LOCK_KEY)")
            }
        }
    } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
        logger.warn("Exercise import interrupted")
        ImportOutcome.FAILED
    } catch (e: Exception) {
        logger.error("Exercise import stopped ({}: {})", e.javaClass.simpleName, e.message)
        ImportOutcome.FAILED
    }

    private fun importAll(checkLock: () -> Unit): Int {
        var failures = 0
        val exercises = checkNotNull(fetch.getExerciseDto()) { "Exercise fetch failed" }
        exercises.forEachIndexed { index, dto ->
            checkLock()
            val id = try { persistence.save(dto) } catch (e: IllegalArgumentException) {
                failures++
                logger.warn("Invalid exercise at index {}", index)
                return@forEachIndexed
            }
            dto.images.forEachIndexed { position, path ->
                val url = try { imageUrl(config.url, dto.id, path) } catch (e: IllegalArgumentException) {
                    failures++
                    logger.warn("Invalid image path for exercise {}, position {}", id, position)
                    return@forEachIndexed
                }
                val publicId = "gym-tracker/exercises/${dto.id}/$position"
                if (!persistence.hasImage(id, publicId)) {
                    val uploaded = uploadWithRetry(url, publicId, id, position, checkLock)
                    if (uploaded == null) failures++
                    else persistence.saveImage(id, publicId, uploaded, position)
                }
            }
            logger.info("Imported exercise {}/{}; failures={}", index + 1, exercises.size, failures)
        }
        return failures
    }

    private fun uploadWithRetry(url: String, publicId: String, id: Long, position: Int, checkLock: () -> Unit): String? {
        for (attempt in 0..2) {
            checkLock()
            Thread.sleep(250L + 1000L * attempt)
            try {
                return uploader.upload(url, publicId)
            } catch (e: InterruptedException) {
                throw e
            } catch (e: Exception) {
                logger.warn("Image upload failed for exercise {}, position {}, attempt {} ({})",
                    id, position, attempt + 1, e.javaClass.simpleName)
            }
        }
        return null
    }

    companion object {
        const val EXTERNAL_SOURCE = "free-exercise-db"
        private const val LOCK_KEY = 724916328

        fun imageUrl(base: String, externalId: String, path: String): String {
            require(externalId.matches(Regex("[A-Za-z0-9_-]+")))
            require(path.matches(Regex(Regex.escape(externalId) + "/[A-Za-z0-9_-]+\\.(jpg|jpeg|png)")))
            require(base.startsWith("https://"))
            return base.trimEnd('/') + "/exercises/" + path
        }
    }
}
