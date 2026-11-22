package com.gymtracker.backend.service.impl

import com.gymtracker.backend.database.repository.UserEntityRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userEntityRepository: UserEntityRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userEntityRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("User not found")

        return User.builder()
            .username(user.email)
            .password(user.password)
            .roles(*user.roles.map { it.name }.toTypedArray())
            .build()
    }
}
