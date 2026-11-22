package com.gymtracker.backend.database.repository

import com.gymtracker.backend.database.model.MuscleGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MuscleGroupRepository : JpaRepository<MuscleGroup, Long> {
}