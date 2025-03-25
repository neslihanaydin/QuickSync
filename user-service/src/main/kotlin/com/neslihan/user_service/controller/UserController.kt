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
import org.springframework.web.bind.annotation.CookieValue
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
        val tokenCookieResponse = userService.register(registerRequest)
        addCookies(response, tokenCookieResponse)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @PostMapping("/login")
    fun login(@RequestBody @Valid loginRequest: LoginRequest, response: HttpServletResponse): ResponseEntity<Any> {
        val tokenCookieResponse = userService.login(loginRequest)
        addCookies(response, tokenCookieResponse)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/refresh")
    fun refreshToken(@CookieValue("refreshToken") refreshToken: String, response: HttpServletResponse): ResponseEntity<Any> {
        val tokenCookieResponse = userService.refreshToken(refreshToken)
        addCookies(response, tokenCookieResponse)
        return ResponseEntity.ok().build()
    }

    private fun addCookies(response: HttpServletResponse, tokenCookieResponse: TokenCookieResponse) {
        response.addCookie(tokenCookieResponse.tokenCookie)
        response.addCookie(tokenCookieResponse.refreshTokenCookie)
    }

    @PostMapping("/logout")
    fun logout(@CookieValue("token") token: String, @CookieValue("refreshToken") refreshToken: String, response: HttpServletResponse): ResponseEntity<Any> {
        val tokenCookieResponse = userService.logout()
        addCookies(response, tokenCookieResponse)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/profile")
    fun getProfile(@CookieValue("token", required = false) token: String?,
                   @CookieValue("refreshToken", required = false) refreshToken: String?,
                   response: HttpServletResponse): ResponseEntity<ProfileResponse> {
        if (token.isNullOrBlank() || refreshToken.isNullOrBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        val profileResponse = userService.getProfile(token, refreshToken)
        var tokens = profileResponse.tokenCookieResponse
        if (tokens != null) {
            addCookies(response, tokens)
        }

        return ResponseEntity.ok(profileResponse.profile)
    }
}