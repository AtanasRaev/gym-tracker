package com.gymtracker.backend.web

import com.gymtracker.backend.database.dto.UserResponseDto
import com.gymtracker.backend.service.AuthenticationService
import com.gymtracker.backend.web.dto.auth.RegisterUserDto
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthenticationController(
    private val authenticationService: AuthenticationService,
    private val sessionAuthenticator: SessionAuthenticator
) {
    /** Creates the account and logs it in. The client must fetch a new CSRF token after this call. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @Valid @RequestBody registerUserDto: RegisterUserDto,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): UserResponseDto {
        val user = authenticationService.register(registerUserDto)
        sessionAuthenticator.login(user.email, registerUserDto.password, request, response)
        return user
    }

    /** The logged-in user, or 401 when there is no session. */
    @GetMapping("/me")
    fun me(authentication: Authentication): UserResponseDto =
        authenticationService.getByEmail(authentication.name)
}
