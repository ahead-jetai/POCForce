package com.example.aicrmdash.dto

import com.example.aicrmdash.domain.UserRole
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Response DTO for user information.
 */
data class UserResponse(
    @Schema(description = "User ID", example = "1")
    val id: Long,
    
    @Schema(description = "User full name", example = "John Doe")
    val name: String,
    
    @Schema(description = "User email address", example = "john.doe@example.com")
    val email: String,
    
    @Schema(description = "User role", example = "SE")
    val role: UserRole
)
