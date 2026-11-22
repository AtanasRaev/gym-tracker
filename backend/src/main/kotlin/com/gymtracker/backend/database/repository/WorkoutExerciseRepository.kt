package com.gymtracker.backend.database.repository

import com.gymtracker.backend.database.model.WorkoutExercise
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WorkoutExerciseRepository : JpaRepository<WorkoutExercise, Long> {
}