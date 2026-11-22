package com.gymtracker.backend.database.repository

import com.gymtracker.backend.database.model.TrainingProgram
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TrainingProgramRepository : JpaRepository<TrainingProgram, Long> {
}