package com.neslihan.user_service.dto

import jakarta.servlet.http.Cookie


data class TokenCookieResponse(
    val tokenCookie: Cookie,
    val refreshTokenCookie: Cookie
)
