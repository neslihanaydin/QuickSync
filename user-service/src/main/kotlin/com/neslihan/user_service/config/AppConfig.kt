package com.neslihan.user_service.config

import io.github.cdimascio.dotenv.Dotenv
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import javax.crypto.SecretKey

@Configuration
class AppConfig {

    private val dotenv: Dotenv = Dotenv.configure().directory("user-service").load()

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun jwtSecretKey(): SecretKey {
        val secretKey = dotenv["JWT_SECRET"] ?: throw IllegalStateException("JWT_SECRET not found. Make sure that a .env file is created in the project directory and that it contains JWT_SECRET.")
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey))
    }
}