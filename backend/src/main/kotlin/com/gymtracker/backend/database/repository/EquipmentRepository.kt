package com.gymtracker.backend.database.repository

import com.gymtracker.backend.database.model.Equipment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EquipmentRepository : JpaRepository<Equipment, Long> {
}