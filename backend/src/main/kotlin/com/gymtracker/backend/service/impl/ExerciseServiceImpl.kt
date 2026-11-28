package com.gymtracker.backend.service.impl

import com.gymtracker.backend.database.model.Exercise
import com.gymtracker.backend.database.repository.ExerciseRepository
import com.gymtracker.backend.service.ExerciseService
import com.gymtracker.backend.web.dto.exercise.ExerciseResponseDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExerciseServiceImpl(
    private val exerciseRepository: ExerciseRepository
) : ExerciseService {

    @Transactional(readOnly = true)
    override fun findAll(pageable: Pageable): Page<ExerciseResponseDto> =
        exerciseRepository.findAll(pageable).map { it.toDto() }

    private fun Exercise.toDto() = ExerciseResponseDto(
        id = requireNotNull(id),
        name = name,
        category = category?.name,
        level = level?.name,
        equipment = equipment?.name,
        force = force?.name,
        mechanic = mechanic?.name,
        primaryMuscles = primaryMuscles.map { it.name }.sorted(),
        secondaryMuscles = secondaryMuscles.map { it.name }.sorted(),
        imageUrls = images.map { it.url },
        instructions = instructions
    )
}
