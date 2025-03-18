package com.neslihan.user_service.security

import com.neslihan.user_service.model.User
import io.jsonwebtoken.Jwts
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtSecretKey: SecretKey,
    @Value("\${jwt.expiration:36000000}") private val expirationInMs: Long
) {
    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    // username + secret + expiration -> signed token
    fun generateToken(user: User): String {
        logger.debug("Generating token for user: {}", user.username)
        return Jwts.builder()
            .subject(user.username)
            .expiration(Date(System.currentTimeMillis() + expirationInMs))
            .issuedAt(Date())
            .signWith(jwtSecretKey)
            .compact()
    }
}