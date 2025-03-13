package com.neslihan.user_service.security

import jakarta.servlet.http.Cookie
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtRequestFilter: JwtRequestFilter,
    private val customOidcUserService: CustomOidcUserService,
    private val jwtTokenProvider: JwtTokenProvider
) {
    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity) : SecurityFilterChain {
        httpSecurity
            .csrf { csrf: CsrfConfigurer<HttpSecurity> -> csrf.disable() }
            .cors { cors: CorsConfigurer<HttpSecurity> -> cors.disable() }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(HttpMethod.POST, "/users/register", "/users/login").permitAll()
                    .requestMatchers("/", "/public/**", "/error/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)
            .oauth2Login { oauth2 ->
                oauth2
                    .userInfoEndpoint { info ->
                        info.oidcUserService(customOidcUserService)
                    }
                    .successHandler { request, response, authentication ->
                        val user = authentication.principal as OAuth2User
                        val token = jwtTokenProvider.generateTokenFromOAuth(user)

                        // Create a cookie with the token
                        val cookie = Cookie("jwtToken", token).apply {
                            path = "/"
                            isHttpOnly = true
                            secure = false
                            maxAge = 3600
                        }
                        //response.addCookie(cookie)
                        response.sendRedirect("http://localhost:3000/?token=$token")
                    }
            }
        return httpSecurity.build()
    }
}