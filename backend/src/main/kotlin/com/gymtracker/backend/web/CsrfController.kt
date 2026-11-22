package com.gymtracker.backend.web

import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class CsrfController {

    @GetMapping("/csrf")
    fun csrf(token: CsrfToken): CsrfToken = token
}