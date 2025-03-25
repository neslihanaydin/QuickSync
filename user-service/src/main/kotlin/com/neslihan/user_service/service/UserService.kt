package com.neslihan.user_service.service

import com.neslihan.user_service.dto.*
import com.neslihan.user_service.model.User
import com.neslihan.user_service.repository.UserRepository
import com.neslihan.user_service.security.JwtTokenProvider
import com.neslihan.user_service.security.JwtTokenValidator
import jakarta.servlet.http.Cookie
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtTokenValidator: JwtTokenValidator
) {

    fun findByUsername(username: String): User? = userRepository.findByUsername(username)

    fun findByEmail(email: String): User? = userRepository.findByEmail(email)

    fun register(request: RegisterRequest): TokenCookieResponse {
        ensureUsernameAvailable(request.username)
        val user = createUser(request)
        val savedUser = userRepository.save(user)
        val token = jwtTokenProvider.generateToken(savedUser)
        val refreshToken = jwtTokenProvider.generateRefreshToken(savedUser)
        return TokenCookieResponse(
            tokenCookie = generateTokenCookie(token),
            refreshTokenCookie = generateRefreshTokenCookie(refreshToken)
        )
    }

    fun login(request: LoginRequest): TokenCookieResponse {
        val user = findByUsername(request.username)
            ?: throw IllegalArgumentException("User not found.")
        if (!validatePassword(request.password, user.password)) {
            throw IllegalArgumentException("Invalid password.")
        }
        val token = jwtTokenProvider.generateToken(user)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user)
        return TokenCookieResponse(
            tokenCookie = generateTokenCookie(token),
            refreshTokenCookie = generateRefreshTokenCookie(refreshToken)
        )
    }

    fun validatePassword(rawPassword: String, hashedPassword: String): Boolean = passwordEncoder.matches(rawPassword, hashedPassword)

    fun logout(): TokenCookieResponse{
        return TokenCookieResponse(
            tokenCookie = generateTokenCookie("", 0),
            refreshTokenCookie = generateRefreshTokenCookie("", 0)
        )
    }

    fun getProfile(token: String, refreshToken: String): ProfileWithCookieResponse {
        var extractedUsername = jwtTokenValidator.extractUsername(token)
        var refreshTokenUsername: String? = null
        if (extractedUsername == null) {
            println("Token is expired. Extracting username from refresh token...")
            refreshTokenUsername = jwtTokenValidator.extractUsername(refreshToken)
        }
        extractedUsername = extractedUsername ?: refreshTokenUsername
        if (extractedUsername == null) {
            println("Refresh token is expired. Invalidating session...")
            logout()
        }

        val user = findByUsername(extractedUsername!!) ?: throw IllegalArgumentException("User not found")
        if (!jwtTokenValidator.validateToken(token, user)) {
            println("Refreshing token...")

            val cookieResponse = refreshToken(refreshToken)

            val newToken = cookieResponse.tokenCookie.value
            val newUsername = jwtTokenValidator.extractUsername(newToken)
                ?: throw IllegalArgumentException("New token is invalid")

            val refreshedUser = findByUsername(newUsername) ?: throw IllegalArgumentException("User not found")

            val profileResponse = ProfileResponse(
                username = refreshedUser.username,
                email = refreshedUser.email
            )

            return ProfileWithCookieResponse(
                profile = profileResponse,
                tokenCookieResponse = cookieResponse
            )
        } else {
            val profileResponse = ProfileResponse(
                username = user.username,
                email = user.email
            )
            return ProfileWithCookieResponse(
                profile = profileResponse,
                tokenCookieResponse = null
            )
        }
    }

    fun refreshToken(refreshToken: String): TokenCookieResponse{
        val username = jwtTokenValidator.extractUsername(refreshToken)
            ?: return logout()

        val user = findByUsername(username) ?: return logout()

        if (!jwtTokenValidator.validateToken(refreshToken, user)) {
            return logout()
        }

        val newAccessToken = jwtTokenProvider.generateToken(user)
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(user)

        return TokenCookieResponse(
            tokenCookie = generateTokenCookie(newAccessToken),
            refreshTokenCookie = generateRefreshTokenCookie(newRefreshToken)
        )
    }

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

    fun generateTokenCookie(token: String, age: Int? = null): Cookie {
        val maxAge = age ?: (jwtTokenProvider.expirationInMs / 1000).toInt()
        return Cookie("token", token).apply {
            isHttpOnly = true
            path = "/"
            maxAge
        }
    }

    fun generateRefreshTokenCookie(refreshToken: String, age: Int? = null): Cookie {
        val maxAge = age ?: (jwtTokenProvider.refreshExpirationInMs / 1000).toInt()
        return Cookie("refreshToken", refreshToken).apply {
            isHttpOnly = true
            path = "/"
            maxAge
        }
    }


    fun generateUsername(email: String): String {
        val parsedEmail = email.substring(0, email.indexOf('@')) // parse email
        val randomEmailSuffix = UUID.randomUUID().toString().substring(0, 3)
        return "$parsedEmail$randomEmailSuffix"
    }
}