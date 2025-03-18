package com.neslihan.user_service.service

import com.neslihan.user_service.dto.AuthResponse
import com.neslihan.user_service.dto.LoginRequest
import com.neslihan.user_service.dto.RegisterRequest
import com.neslihan.user_service.model.User
import com.neslihan.user_service.repository.UserRepository
import com.neslihan.user_service.security.JwtTokenProvider
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {
    fun registerOAuth2User(request: RegisterRequest): String {
        ensureUsernameAvailable(request.username)
        val user = createUser(request)
        val savedUser = userRepository.save(user)
        return jwtTokenProvider.generateToken(savedUser)
    }

    fun register(request: RegisterRequest, response: HttpServletResponse): AuthResponse {
        ensureUsernameAvailable(request.username)
        val user = createUser(request)
        val savedUser = userRepository.save(user)
        val token = jwtTokenProvider.generateToken(savedUser)
        println("token: $token")
        response.addCookie(generateTokenCookie(token))
        println("response: $response")
        return AuthResponse(token)
    }

    fun login(request: LoginRequest, response: HttpServletResponse): AuthResponse {
        val user = findByUsername(request.username)
            ?: throw IllegalArgumentException("User not found.")
        if (!validatePassword(request.password, user.password)) {
            throw IllegalArgumentException("Invalid password.")
        }
        val token = jwtTokenProvider.generateToken(user)
        response.addCookie(generateTokenCookie(token))
        return AuthResponse(token)
    }

    fun logout(): Cookie{
        return Cookie("token", "").apply {
            isHttpOnly = true
            path = "/"
            maxAge = 0
        }
    }

    fun findByUsername(username: String): User? = userRepository.findByUsername(username)

    fun findByEmail(email: String): User? = userRepository.findByEmail(email)

    fun validatePassword(rawPassword: String, hashedPassword: String): Boolean = passwordEncoder.matches(rawPassword, hashedPassword)

    private fun ensureUsernameAvailable(username: String) {
        if (findByUsername(username) != null) {
            throw IllegalArgumentException("Username already exists.")
        }
    }

    private fun createUser(request: RegisterRequest): User {
        return User(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password)
        )
    }

    private fun generateTokenCookie(token: String): Cookie {
        return Cookie("token", token).apply {
            isHttpOnly = true
            path = "/"
            maxAge = 3600
        }
    }
}