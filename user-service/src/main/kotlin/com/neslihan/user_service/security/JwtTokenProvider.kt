package com.neslihan.user_service.security

import com.neslihan.user_service.model.User
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtSecretKey: SecretKey,
    @Value("\${jwt.expiration:36000000}") private val expirationInMs: Long
) {
    // username + secret + expiration -> signed token
    fun generateToken(user: User): String {
        return Jwts.builder()
            .subject(user.username)
            .expiration(Date(System.currentTimeMillis() + expirationInMs))
            .issuedAt(Date())
            .signWith(jwtSecretKey)
            .compact()
    }
}