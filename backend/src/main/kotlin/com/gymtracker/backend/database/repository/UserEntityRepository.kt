package com.gymtracker.backend.database.repository

import com.gymtracker.backend.database.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserEntityRepository : JpaRepository<UserEntity, UUID> {
    // Case-insensitive so that rows saved before email normalization still match
    fun findByEmailIgnoreCase(email: String): UserEntity?

    fun existsByEmailIgnoreCase(email: String): Boolean

    fun existsByUsername(username: String): Boolean

    /**
     * Takes a transaction-scoped Postgres advisory lock. Registrations run one at a time,
     * so the "first user becomes admin" check and the uniqueness checks cannot race.
     */
    @Query(value = "SELECT 1 FROM pg_advisory_xact_lock(:key)", nativeQuery = true)
    fun lockForRegistration(key: Long): Int
}
