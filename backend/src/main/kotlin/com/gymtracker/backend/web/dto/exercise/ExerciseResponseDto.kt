package com.gymtracker.backend.web.dto.exercise

data class ExerciseResponseDto(
    val id: Long,
    val name: String,
    val category: String?,
    val level: String?,
    val equipment: String?,
    val force: String?,
    val mechanic: String?,
    val primaryMuscles: List<String>,
    val secondaryMuscles: List<String>,
    val imageUrls: List<String>,
    val instructions: String?
)
