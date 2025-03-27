package com.neslihan.user_service.config

import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import javax.crypto.SecretKey

@Configuration
class AppConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun jwtSecretKey(): SecretKey {
        val jwtSecret: String = System.getenv("JWT_SECRET") ?:
        throw IllegalStateException("JWT_SECRET not found in environment variables")
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret))
    }
}