package com.gymtracker.backend.database.repository

import com.gymtracker.backend.database.model.SetExercise
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SetExerciseRepository : JpaRepository<SetExercise, Long> {
}