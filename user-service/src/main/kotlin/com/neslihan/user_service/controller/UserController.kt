package com.neslihan.user_service.controller

import com.neslihan.user_service.dto.*
import com.neslihan.user_service.model.User
import com.neslihan.user_service.service.UserService
import jakarta.servlet.http.HttpServletResponse
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
    fun register(@RequestBody @Valid registerRequest: RegisterRequest, response: HttpServletResponse): ResponseEntity<Any> {
        userService.register(registerRequest, response)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @PostMapping("/login")
    fun login(@RequestBody @Valid loginRequest: LoginRequest, response: HttpServletResponse): ResponseEntity<Any> {
        userService.login(loginRequest, response)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/logout")
    fun logout(response: HttpServletResponse): ResponseEntity<Any> {
        val cookie = userService.logout()
        response.addCookie(cookie)
        return ResponseEntity.ok().build()
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