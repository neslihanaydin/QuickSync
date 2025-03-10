package com.neslihan.user_service.controller

import com.neslihan.user_service.model.User
import com.neslihan.user_service.security.JwtTokenProvider
import com.neslihan.user_service.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val jwtTokenProvider: JwtTokenProvider
) {
    data class LoginRequest(val username: String, val password: String)
    data class LoginResponse(val token: String)

    @PostMapping("/register")
    fun register(@RequestBody user: User): ResponseEntity<Any> {
        val existingUser = userService.findByUsername(user.username)
        if (existingUser != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists.")
        }
        val savedUser = userService.registerUser(user)
        return login(LoginRequest(user.username, user.password))
    }

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<Any> {
        val user = userService.findByUsername(loginRequest.username)
        return if (user != null && userService.validatePassword(loginRequest.password, user.password)) {
            val token = jwtTokenProvider.generateToken(user)
            ResponseEntity
                .ok(LoginResponse(token))
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials.")
        }
    }

    @GetMapping("/profile")
    fun getProfile(authentication: Authentication): ResponseEntity<Map<String, Any>> {
        val user = authentication.principal as User
        return ResponseEntity.ok(
            mapOf(
                "username" to user.username,
                "email" to user.email
            )
        )
    }
}