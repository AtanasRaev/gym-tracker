package com.gymtracker.backend.database.dto

import java.util.UUID

data class UserResponseDto(
    val id: UUID,

    val username: String,

    val email: String,

    val firstName: String?,

    val lastName: String?,

    val roles: List<String>
)