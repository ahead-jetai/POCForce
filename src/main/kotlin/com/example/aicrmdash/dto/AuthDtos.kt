package com.example.aicrmdash.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * Request DTO for user login.
 */
data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)

/**
 * User DTO for authentication responses.
 */
data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    val role: String
)

/**
 * Response DTO for successful authentication.
 */
data class LoginResponse(
    val token: String,
    val user: UserDto
)

/**
 * Request DTO for user registration.
 */
data class RegisterRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String,

    @field:NotBlank(message = "Role is required")
    val role: String
)
