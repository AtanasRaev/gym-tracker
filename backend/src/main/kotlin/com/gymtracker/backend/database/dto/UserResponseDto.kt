package com.gymtracker.backend.database.dto

import com.gymtracker.backend.database.model.UserEntity
import java.util.UUID

data class UserResponseDto(
    val id: UUID,

    val username: String,

    val email: String,

    val firstName: String?,

    val lastName: String?,

    val roles: List<String>
)

fun UserEntity.toResponseDto() = UserResponseDto(
    id = id,
    username = username,
    email = email,
    firstName = firstName,
    lastName = lastName,
    roles = roles.map { it.name }.sorted()
)
