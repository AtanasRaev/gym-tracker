package com.gymtracker.backend.service

import com.gymtracker.backend.database.dto.UserResponseDto
import com.gymtracker.backend.web.dto.auth.RegisterUserDto

interface AuthenticationService {
    fun register(registerUserDto: RegisterUserDto): UserResponseDto

    fun getByEmail(email: String): UserResponseDto
}

/** Emails are stored trimmed and lowercase, so lookups and uniqueness ignore case. */
fun normalizeEmail(email: String): String = email.trim().lowercase()
