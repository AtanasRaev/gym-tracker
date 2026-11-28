package com.gymtracker.backend.service.impl

import com.gymtracker.backend.database.model.*
import com.gymtracker.backend.database.model.enums.ExerciseCategory
import com.gymtracker.backend.database.model.enums.ExerciseEquipment
import com.gymtracker.backend.database.model.enums.ExerciseForce
import com.gymtracker.backend.database.model.enums.ExerciseLevel
import com.gymtracker.backend.database.model.enums.ExerciseMechanic
import com.gymtracker.backend.database.repository.*
import com.gymtracker.backend.service.api.dto.ExternalExerciseDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExerciseImportPersistence(
    private val exercises: ExerciseRepository,
    private val muscles: MuscleGroupRepository
) {
    @Transactional
    fun save(dto: ExternalExerciseDto): Long {
        val exercise = exercises.findByExternalSourceAndExternalId(ExerciseImportService.EXTERNAL_SOURCE, dto.id)
            ?: Exercise(name = dto.name, externalId = dto.id, externalSource = ExerciseImportService.EXTERNAL_SOURCE)
        exercise.name = dto.name
        exercise.instructions = dto.instructions.joinToString("\n")
        exercise.level = ExerciseLevel.valueOf(dto.level.uppercase())
        exercise.category = ExerciseCategory.valueOf(dto.category.uppercase().replace(' ', '_'))
        exercise.mechanic = dto.mechanic?.let { ExerciseMechanic.valueOf(it.uppercase()) }
        fun resolve(names: List<String>) = names.map { raw ->
            val name = raw.trim().also { require(it.isNotEmpty()) }
            muscles.findFirstByNameIgnoreCase(name) ?: muscles.save(MuscleGroup(name = name))
        }.toMutableSet()
        exercise.primaryMuscles = resolve(dto.primaryMuscles)
        exercise.secondaryMuscles = resolve(dto.secondaryMuscles)
        exercise.equipment = dto.equipment?.trim()?.takeIf { it.isNotEmpty() }?.let {
            ExerciseEquipment.valueOf(it.uppercase().replace(Regex("[ -]+"), "_"))
        }
        exercise.force = dto.force?.trim()?.takeIf { it.isNotEmpty() }?.let {
            ExerciseForce.valueOf(it.uppercase())
        }
        return exercises.saveAndFlush(exercise).id!!
    }

    @Transactional(readOnly = true)
    fun hasImage(id: Long, publicId: String): Boolean =
        exercises.findById(id).orElseThrow().images.any { it.publicId == publicId }

    @Transactional
    fun saveImage(id: Long, publicId: String, url: String, position: Int) {
        val exercise = exercises.findById(id).orElseThrow()
        if (exercise.images.none { it.publicId == publicId }) {
            exercise.images.add(ExerciseImage(publicId = publicId, url = url, position = position))
            exercises.saveAndFlush(exercise)
        }
    }
}
