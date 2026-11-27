package com.gymtracker.backend.service.impl

import com.gymtracker.backend.config.ApiConfig
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component
import javax.sql.DataSource
import kotlin.system.exitProcess

@Component
@ConditionalOnProperty(name = ["exercise-import.enabled"], havingValue = "true")
class ExerciseImportCommand(
    private val fetch: FetchDbServiceImpl,
    private val persistence: ExerciseImportPersistence,
    private val uploader: ExerciseImageUploader,
    private val config: ApiConfig,
    private val dataSource: DataSource,
    private val context: ConfigurableApplicationContext
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        var failures = 0
        val code = try {
            dataSource.connection.use { lock ->
                fun query(sql: String): Boolean = lock.createStatement().use { statement ->
                    statement.executeQuery(sql).use { it.next(); it.getBoolean(1) }
                }
                check(query("SELECT pg_try_advisory_lock(724916328)")) { "Import already running" }
                try {
                    val exercises = checkNotNull(fetch.getExerciseDto()) { "Exercise fetch failed" }
                    exercises.forEachIndexed { index, dto ->
                        check(lock.isValid(5)) { "Import lock connection lost" }
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
                                var uploaded: String? = null
                                for (attempt in 0..2) {
                                    check(lock.isValid(5)) { "Import lock connection lost" }
                                    Thread.sleep(250L + if (attempt == 0) 0 else 1000L * attempt)
                                    try {
                                        uploaded = uploader.upload(url, publicId)
                                        break
                                    } catch (e: InterruptedException) {
                                        Thread.currentThread().interrupt()
                                        throw e
                                    } catch (e: Exception) {
                                        logger.warn("Image upload failed for exercise {}, position {}, attempt {} ({})",
                                            id, position, attempt + 1, e.javaClass.simpleName)
                                    }
                                }
                                if (uploaded == null) failures++
                                else persistence.saveImage(id, publicId, uploaded, position)
                            }
                        }
                        logger.info("Imported exercise {}/{}; image failures={}", index + 1, exercises.size, failures)
                    }
                } finally { if (!lock.isClosed) query("SELECT pg_advisory_unlock(724916328)") }
            }
            if (failures == 0) 0 else 2
        } catch (e: Exception) {
            logger.error("Exercise import stopped ({})", e.javaClass.simpleName)
            1
        }
        context.close()
        exitProcess(code)
    }

    companion object {
        fun imageUrl(base: String, externalId: String, path: String): String {
            require(externalId.matches(Regex("[A-Za-z0-9_-]+")))
            require(path.matches(Regex(Regex.escape(externalId) + "/[A-Za-z0-9_-]+\\.(jpg|jpeg|png)")))
            require(base.startsWith("https://"))
            return base.trimEnd('/') + "/exercises/" + path
        }
    }
}
