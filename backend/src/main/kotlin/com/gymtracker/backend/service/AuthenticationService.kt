package com.gymtracker.backend.service

import com.gymtracker.backend.database.dto.UserResponseDto
import com.gymtracker.backend.web.dto.auth.RegisterUserDto

interface AuthenticationService {
    fun register(registerUserDto: RegisterUserDto): UserResponseDto
}