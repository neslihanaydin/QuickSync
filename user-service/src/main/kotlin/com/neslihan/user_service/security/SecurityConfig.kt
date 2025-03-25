package com.neslihan.user_service.security

import com.neslihan.user_service.dto.RegisterRequest
import com.neslihan.user_service.dto.TokenCookieResponse
import com.neslihan.user_service.service.UserService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.util.*


@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtRequestFilter: JwtRequestFilter,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService
) {

    val logger = LoggerFactory.getLogger(SecurityConfig::class.java)
    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity) : SecurityFilterChain {
        httpSecurity
            .csrf { csrf: CsrfConfigurer<HttpSecurity> -> csrf.disable() }
            .cors { }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(HttpMethod.POST, "/users/register", "/users/login", "/users/oauth2/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/users/oauth2/**", "/users/profile").permitAll()
                    .requestMatchers("/", "/public/**", "/error/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)
            .oauth2Login { oauth2 ->
                oauth2
                    .authorizationEndpoint { endpoint ->
                        endpoint.baseUri("/users/oauth2/authorization")
                    }
                    .successHandler { request, response, authentication ->
                        handleOAuth2Success(request, response, authentication)
                    }

            }
        return httpSecurity.build()
    }

    private fun handleOAuth2Success(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oAuth2User = authentication.principal as? OAuth2User
            ?: throw IllegalArgumentException("OAuth2User expected")
        val userEmail = oAuth2User.attributes["email"] as? String
            ?: throw IllegalArgumentException("Email not found in OAuth2User attributes")
        
        val tokenCookieResponse: TokenCookieResponse
        val dbUser = userService.findByEmail(userEmail)
        if (dbUser == null) {
            val username = userService.generateUsername(userEmail)
            logger.debug("User not found, creating new user. Username: $username")
            val registerRequest = RegisterRequest(
                username = username,
                email = userEmail,
                password = UUID.randomUUID().toString() // unusable password
            )
             tokenCookieResponse = userService.register(registerRequest)
        } else {
            val token = jwtTokenProvider.generateToken(dbUser)
            val refreshToken = jwtTokenProvider.generateRefreshToken(dbUser)
            tokenCookieResponse = TokenCookieResponse(
                tokenCookie = userService.generateTokenCookie(token),
                refreshTokenCookie = userService.generateRefreshTokenCookie(refreshToken)
            )
        }
        response.addCookie(tokenCookieResponse.tokenCookie)
        response.addCookie(tokenCookieResponse.refreshTokenCookie)
        response.sendRedirect("http://localhost:3000/")
    }
}