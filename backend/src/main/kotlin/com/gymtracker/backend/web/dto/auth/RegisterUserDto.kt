package com.gymtracker.backend.web.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterUserDto(
    @field:NotBlank(message = "Username cannot be empty")
    @field:Size(
        min = 3,
        max = 30,
        message = "Username must be between 3 and 30 characters"
    )
    val username: String,

    @field:NotBlank(message = "Password cannot be empty")
    @field:Size(
        min = 8,
        message = "Password must be at least 8 characters"
    )
    val password: String,

    @field:NotBlank(message = "Email cannot be empty")
    @field:Email(message = "Invalid email format")
    @field:Size(max = 254)
    val email: String,

    @field:Size(max = 100)
    val firstName: String? = null,

    @field:Size(max = 100)
    val lastName: String? = null
)
