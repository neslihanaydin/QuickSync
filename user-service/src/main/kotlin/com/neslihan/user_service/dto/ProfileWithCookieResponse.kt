package com.neslihan.user_service.dto

data class ProfileWithCookieResponse(
    val profile: ProfileResponse,
    val tokenCookieResponse: TokenCookieResponse? = null
)

