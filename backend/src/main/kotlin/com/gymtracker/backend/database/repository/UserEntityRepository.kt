package com.gymtracker.backend.database.repository

import com.gymtracker.backend.database.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserEntityRepository : JpaRepository<UserEntity, UUID>