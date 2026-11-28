package com.gymtracker.backend.service.impl

import com.gymtracker.backend.database.dto.UserResponseDto
import com.gymtracker.backend.database.dto.toResponseDto
import com.gymtracker.backend.database.model.UserEntity
import com.gymtracker.backend.database.model.enums.Role
import com.gymtracker.backend.database.repository.UserEntityRepository
import com.gymtracker.backend.exception.FieldException
import com.gymtracker.backend.service.AuthenticationService
import com.gymtracker.backend.service.normalizeEmail
import com.gymtracker.backend.web.dto.auth.RegisterUserDto
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class AuthenticationServiceImpl(
    private val userEntityRepository: UserEntityRepository,
    private val passwordEncoder: PasswordEncoder
) : AuthenticationService {

    @Transactional
    override fun register(registerUserDto: RegisterUserDto): UserResponseDto {
        // The DTO limits characters; BCrypt limits UTF-8 bytes
        if (registerUserDto.password.toByteArray(Charsets.UTF_8).size > MAX_PASSWORD_BYTES) {
            throw FieldException(HttpStatus.BAD_REQUEST, "password", "Password is too long")
        }

        val email = normalizeEmail(registerUserDto.email)
        val username = registerUserDto.username.trim()

        userEntityRepository.lockForRegistration(REGISTRATION_LOCK_KEY)

        if (userEntityRepository.existsByEmailIgnoreCase(email)) {
            throw FieldException(HttpStatus.CONFLICT, "email", "Email already registered")
        }

        if (userEntityRepository.existsByUsername(username)) {
            throw FieldException(HttpStatus.CONFLICT, "username", "Username already taken")
        }

        val encodedPassword = passwordEncoder.encode(registerUserDto.password)
            ?: throw IllegalStateException("Password encoding failed")

        // The first account becomes the admin. Admins store every role explicitly.
        val roles = if (userEntityRepository.count() == 0L) {
            Role.entries.toMutableSet()
        } else {
            mutableSetOf(Role.USER)
        }

        val user = UserEntity(
            username = username,
            email = email,
            password = encodedPassword,
            firstName = registerUserDto.firstName?.trim()?.ifEmpty { null },
            lastName = registerUserDto.lastName?.trim()?.ifEmpty { null },
            roles = roles
        )

        try {
            userEntityRepository.saveAndFlush(user)
        } catch (e: DataIntegrityViolationException) {
            // Safety net if a row was written outside this lock (for example, by hand)
            throw ResponseStatusException(HttpStatus.CONFLICT, "Email or username already in use", e)
        }

        return user.toResponseDto()
    }

    @Transactional(readOnly = true)
    override fun getByEmail(email: String): UserResponseDto =
        userEntityRepository.findByEmailIgnoreCase(normalizeEmail(email))?.toResponseDto()
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

    private companion object {
        const val MAX_PASSWORD_BYTES = 72

        // Arbitrary constant that identifies the registration lock in pg_advisory_xact_lock
        const val REGISTRATION_LOCK_KEY = 4_815_162_342L
    }
}
