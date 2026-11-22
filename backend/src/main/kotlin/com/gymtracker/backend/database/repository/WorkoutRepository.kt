package com.gymtracker.backend.database.repository

import com.gymtracker.backend.database.model.Workout
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WorkoutRepository : JpaRepository<Workout, Long> {
}