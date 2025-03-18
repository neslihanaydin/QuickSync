package com.neslihan.user_service.security

import com.neslihan.user_service.dto.RegisterRequest
import com.neslihan.user_service.service.UserService
import jakarta.servlet.http.Cookie
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
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
                    .requestMatchers(HttpMethod.GET, "/users/oauth2/**").permitAll()
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
                        val user = authentication.principal as OAuth2User

                        val userEmail = user.attributes["email"] as String
                        var dbUser = userService.findByEmail(userEmail)

                        var token = ""
                        if (dbUser == null) {
                            val parsedEmail = userEmail.substring(0, userEmail.indexOf('@')) // parse email
                            val randomSuffixtoEmail = UUID.randomUUID().toString().substring(0, 3)
                            val username = "$parsedEmail$randomSuffixtoEmail"
                            logger.debug("User not found, creating new user inside successHandler...")
                            logger.debug("User name: $username")
                            val registerRequest =
                                RegisterRequest(
                                    username = username,
                                    email = userEmail,
                                    password = UUID.randomUUID().toString() // unusable password
                                )
                            token = userService.registerOAuth2User(registerRequest)
                        } else {
                            token = jwtTokenProvider.generateToken(dbUser)
                        }

                        logger.debug("Token: $token")

                        // token as cookie
                        val tokenCookie = Cookie("token", token)
                        tokenCookie.isHttpOnly = true
                        tokenCookie.path = "/"
                        tokenCookie.maxAge = 3600
                        response.addCookie(tokenCookie)

                        response.sendRedirect("http://localhost:3000/?token=$token")
                    }

            }
        return httpSecurity.build()
    }
}