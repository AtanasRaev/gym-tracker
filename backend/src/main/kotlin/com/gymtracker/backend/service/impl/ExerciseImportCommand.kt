package com.gymtracker.backend.service.impl

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

/** One-off import: run with --exercise-import.enabled=true, then the process exits. */
@Component
@ConditionalOnProperty(name = ["exercise-import.enabled"], havingValue = "true")
class ExerciseImportCommand(
    private val importService: ExerciseImportService,
    private val context: ConfigurableApplicationContext
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        val code = when (importService.runImport()) {
            ImportOutcome.SUCCESS -> 0
            ImportOutcome.PARTIAL -> 2
            ImportOutcome.ALREADY_RUNNING, ImportOutcome.FAILED -> 1
        }
        context.close()
        exitProcess(code)
    }
}
