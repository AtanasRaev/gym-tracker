package com.gymtracker.backend.service.impl

import com.gymtracker.backend.database.repository.ExerciseRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Seeds the exercise catalog on first start. Once the server is ready, if no
 * free-exercise-db exercises exist yet, the import runs on a background thread
 * so startup is never blocked and a failed import never takes the server down.
 * To fill in after a partial run, use the one-off command (see IMPORT.md).
 */
@Component
@ConditionalOnProperty(name = ["exercise-import.on-startup"], havingValue = "true", matchIfMissing = true)
class ExerciseStartupSeeder(
    private val exercises: ExerciseRepository,
    private val importService: ExerciseImportService,
    @Value("\${exercise-import.enabled:false}") private val commandMode: Boolean
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    fun seedIfEmpty() {
        if (commandMode) return // the one-off command runs the import itself
        if (exercises.existsByExternalSource(ExerciseImportService.EXTERNAL_SOURCE)) {
            logger.info("Exercise catalog already present; skipping startup import")
            return
        }
        logger.info("Exercise catalog empty; starting background import")
        Thread.ofPlatform().name("exercise-import").daemon(true).start {
            logger.info("Startup exercise import finished: {}", importService.runImport())
        }
    }
}
