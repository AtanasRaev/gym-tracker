package com.gymtracker.backend.service.impl

import com.gymtracker.backend.database.dto.UserResponseDto
import com.gymtracker.backend.database.model.enums.Role
import com.gymtracker.backend.database.model.UserEntity
import com.gymtracker.backend.database.repository.UserEntityRepository
import com.gymtracker.backend.service.AuthenticationService
import com.gymtracker.backend.web.dto.auth.RegisterUserDto
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthenticationServiceImpl(
    private val userEntityRepository: UserEntityRepository,
    private val passwordEncoder: PasswordEncoder
) : AuthenticationService {

    override fun register(registerUserDto: RegisterUserDto): UserResponseDto {
        if (userEntityRepository.existsByEmail(registerUserDto.email)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Email already registered"
            )
        }

        if (userEntityRepository.existsByUsername(registerUserDto.username)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Username already taken"
            )
        }

        val encodedPassword = passwordEncoder.encode(registerUserDto.password)
            ?: throw IllegalStateException("Password encoding failed")

        val user = UserEntity(
            username = registerUserDto.username,
            email = registerUserDto.email,
            password = encodedPassword,
            firstName = registerUserDto.firstName,
            lastName = registerUserDto.lastName,
            roles = mutableSetOf(Role.USER)
        )

        userEntityRepository.save(user)

        return UserResponseDto(
            id = user.id,
            username = user.username,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            roles = user.roles.map { it.name }
        )
    }
}