package com.neslihan.user_service.security

import com.neslihan.user_service.model.User
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenValidator(
    private val jwtSecretKey: SecretKey
) {
    fun extractUsername(token: String): String? = runCatching {
        Jwts.parser()
            .verifyWith(jwtSecretKey)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
    }.getOrElse { null }

    private fun isTokenExpired(token: String): Boolean = runCatching {
        val expiration = Jwts.parser()
            .verifyWith(jwtSecretKey)
            .build()
            .parseSignedClaims(token)
            .payload
            .expiration
        expiration.before(Date())
    }.getOrElse { true }

    fun validateToken(token: String, user: User): Boolean {
        val username = extractUsername(token)
        return username == user.username && !isTokenExpired(token)
    }
}