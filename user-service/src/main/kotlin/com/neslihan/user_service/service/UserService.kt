package com.neslihan.user_service.service

import com.neslihan.user_service.dto.AuthResponse
import com.neslihan.user_service.dto.LoginRequest
import com.neslihan.user_service.dto.RegisterRequest
import com.neslihan.user_service.model.User
import com.neslihan.user_service.repository.UserRepository
import com.neslihan.user_service.security.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {
    fun registerOAuth2User(request: RegisterRequest): String {
        val existingUser = findByUsername(request.username)
        if (existingUser != null) {
            throw IllegalArgumentException("Username already exists.")
        }

        val user = User(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password)
        )

        val savedUser = userRepository.save(user)

        val token = jwtTokenProvider.generateToken(savedUser)

        return token
    }

    fun register(request: RegisterRequest): AuthResponse {
        val existingUser = findByUsername(request.username)
        if (existingUser != null) {
            throw IllegalArgumentException("Username already exists.")
        }

        val user = User(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password)
        )

        val savedUser = userRepository.save(user)

        val token = jwtTokenProvider.generateToken(savedUser)

        return AuthResponse(token)
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = findByUsername(request.username)
        return if (user != null && validatePassword(request.password, user.password)) {
            val token = jwtTokenProvider.generateToken(user)
            AuthResponse(token)
        } else {
            throw IllegalArgumentException("Invalid credentials.")
        }
    }

    fun findByUsername(username: String): User? = userRepository.findByUsername(username)

    fun findByEmail(email: String): User? = userRepository.findByEmail(email)

    fun validatePassword(rawPassword: String, hashedPassword: String): Boolean = passwordEncoder.matches(rawPassword, hashedPassword)
}