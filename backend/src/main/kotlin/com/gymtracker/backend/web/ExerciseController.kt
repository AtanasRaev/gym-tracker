package com.gymtracker.backend.web

import com.gymtracker.backend.service.ExerciseService
import com.gymtracker.backend.web.dto.exercise.ExerciseResponseDto
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.PagedModel
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/exercises")
class ExerciseController(
    private val exerciseService: ExerciseService
) {
    /** Pageable list: `?page=0&size=20&sort=name,asc`. */
    @GetMapping
    fun findAll(
        @PageableDefault(size = 20, sort = ["name"], direction = Sort.Direction.ASC) pageable: Pageable
    ): PagedModel<ExerciseResponseDto> = PagedModel(exerciseService.findAll(pageable))
}
