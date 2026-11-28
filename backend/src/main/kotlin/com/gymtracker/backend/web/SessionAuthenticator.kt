package com.gymtracker.backend.web

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy
import org.springframework.security.web.csrf.CsrfTokenRepository
import org.springframework.stereotype.Component

/**
 * Logs a user in from a controller, with the same session steps that formLogin applies:
 * a new session ID (session fixation protection), a new CSRF token, and a saved security context.
 */
@Component
class SessionAuthenticator(
    private val authenticationManager: AuthenticationManager,
    private val securityContextRepository: SecurityContextRepository,
    csrfTokenRepository: CsrfTokenRepository
) {
    private val sessionStrategy = CompositeSessionAuthenticationStrategy(
        listOf(
            ChangeSessionIdAuthenticationStrategy(),
            CsrfAuthenticationStrategy(csrfTokenRepository)
        )
    )

    private val contextHolderStrategy = SecurityContextHolder.getContextHolderStrategy()

    fun login(
        email: String,
        password: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(email, password)
        )

        sessionStrategy.onAuthentication(authentication, request, response)

        val context = contextHolderStrategy.createEmptyContext()
        context.authentication = authentication
        contextHolderStrategy.context = context
        securityContextRepository.saveContext(context, request, response)
    }
}
