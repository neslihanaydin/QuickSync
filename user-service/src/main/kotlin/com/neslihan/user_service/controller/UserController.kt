package com.neslihan.user_service.controller

import com.neslihan.user_service.dto.*
import com.neslihan.user_service.model.User
import com.neslihan.user_service.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {
    val logger = org.slf4j.LoggerFactory.getLogger(UserController::class.java)
    @PostMapping("/register")
    fun register(@RequestBody @Valid registerRequest: RegisterRequest): ResponseEntity<Any> {
        val authResponse = userService.register(registerRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse)
    }

    @PostMapping("/login")
    fun login(@RequestBody @Valid loginRequest: LoginRequest): ResponseEntity<Any> {
        val authResponse = userService.login(loginRequest)
        return ResponseEntity.ok(authResponse)
    }

    @GetMapping("/profile")
    fun getProfile(authentication: Authentication): ResponseEntity<ProfileResponse> {
        val principal = authentication.principal
        logger.debug("Principal: $principal")
        val profileResponse = when (principal) {
            is User -> ProfileResponse(
                username = principal.username,
                email = principal.email
            )
            is OidcUser ->
                ProfileResponse(
                username = principal.getAttribute<String>("email").toString(),
                email = principal.getAttribute<String>("email").toString()
            )
            else -> throw IllegalArgumentException("Invalid token.")
        }
        logger.debug("Profile response: $profileResponse")
        return ResponseEntity.ok(profileResponse)
    }
}