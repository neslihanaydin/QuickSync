package com.neslihan.user_service.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class RegisterRequest(
    @field:NotBlank(message = "Username is required")
    val username: String,

    @field:Email(message = "Email is not valid")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)