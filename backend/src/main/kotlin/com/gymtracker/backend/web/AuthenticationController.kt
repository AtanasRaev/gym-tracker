package com.gymtracker.backend.web

import com.gymtracker.backend.web.dto.auth.RegisterUserDto
import com.gymtracker.backend.service.AuthenticationService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthenticationController(
    private val authenticationService: AuthenticationService
) {
    @PostMapping("/register")
    fun register(@Valid @RequestBody registerUserDto: RegisterUserDto)
    = authenticationService.register(registerUserDto)
}