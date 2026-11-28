package com.gymtracker.backend.config

import com.gymtracker.backend.service.AuthenticationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.security.web.csrf.CsrfTokenRepository
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import tools.jackson.databind.json.JsonMapper

@Configuration
class SecurityConfig {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        securityContextRepository: SecurityContextRepository,
        csrfTokenRepository: CsrfTokenRepository,
        authenticationService: AuthenticationService,
        jsonMapper: JsonMapper
    ): SecurityFilterChain {

        http
            .cors { }
            .csrf { csrf -> csrf.csrfTokenRepository(csrfTokenRepository) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/auth/register",
                        "/auth/login",
                        "/auth/csrf",
                        "/actuator/health",
                        // Spring forwards errors here. Without this, anonymous callers get 401 for every error.
                        "/error"
                    ).permitAll()

                    // The exercise catalog is public reference data
                    .requestMatchers(HttpMethod.GET, "/api/exercises", "/api/exercises/**")
                    .permitAll()

                    .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")

                    .requestMatchers("/api/**")
                    .hasRole("USER")

                    .anyRequest()
                    .authenticated()
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.IF_REQUIRED
                )
            }
            .securityContext { context ->
                context.securityContextRepository(
                    securityContextRepository
                )
            }
            .formLogin { form ->
                form
                    .loginProcessingUrl("/auth/login")
                    .usernameParameter("email")
                    .passwordParameter("password")
                    // Return the user so the SPA can render without a second request.
                    // The CSRF token changes on login, so the client must fetch /auth/csrf again.
                    .successHandler { _, response, authentication ->
                        val user = authenticationService.getByEmail(authentication.name)
                        response.status = HttpStatus.OK.value()
                        response.contentType = MediaType.APPLICATION_JSON_VALUE
                        jsonMapper.writeValue(response.outputStream, user)
                    }
                    .failureHandler { _, response, _ ->
                        response.status = HttpStatus.UNAUTHORIZED.value()
                    }
                    .permitAll()
            }
            .httpBasic { it.disable() }
            .exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint(
                    HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                )
            }
            .logout { logout ->
                logout
                    .logoutUrl("/auth/logout")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID")
                    .logoutSuccessHandler { _, response, _ ->
                        response.status =
                            HttpStatus.NO_CONTENT.value()
                    }
            }

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder =
        BCryptPasswordEncoder()

    @Bean
    fun securityContextRepository(): SecurityContextRepository =
        HttpSessionSecurityContextRepository()

    // Shared with SessionAuthenticator, which rotates the token after registration
    @Bean
    fun csrfTokenRepository(): CsrfTokenRepository =
        HttpSessionCsrfTokenRepository()

    @Bean
    fun authenticationManager(
        configuration: AuthenticationConfiguration
    ): AuthenticationManager =
        configuration.authenticationManager

    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {

        val config = CorsConfiguration().apply {
            allowedOrigins = listOf(
                "http://localhost:3000",
                "http://localhost:5173"
            )

            allowedMethods = listOf(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
            )

            allowedHeaders = listOf(
                "Content-Type",
                "X-CSRF-TOKEN"
            )

            allowCredentials = true
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}
