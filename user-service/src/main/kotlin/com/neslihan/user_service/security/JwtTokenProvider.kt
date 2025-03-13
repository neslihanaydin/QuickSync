package com.neslihan.user_service.security

import com.neslihan.user_service.model.User
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.core.user.OAuth2User
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
    fun generateTokenFromOAuth(oAuth2User: OAuth2User): String {
        val email = oAuth2User.getAttribute<String>("email")
            ?: throw IllegalArgumentException("Email attribute not found in OAuth2 response")

        return Jwts.builder()
            .subject(email)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expirationInMs))
            .signWith(jwtSecretKey)
            .compact()
    }

}