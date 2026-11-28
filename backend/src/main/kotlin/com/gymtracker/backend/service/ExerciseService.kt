package com.gymtracker.backend.service

import com.gymtracker.backend.web.dto.exercise.ExerciseResponseDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ExerciseService {
    fun findAll(pageable: Pageable): Page<ExerciseResponseDto>
}
