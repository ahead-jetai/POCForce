package com.example.aicrmdash.controller

import com.example.aicrmdash.dto.LoginRequest
import com.example.aicrmdash.dto.LoginResponse
import com.example.aicrmdash.dto.RegisterRequest
import com.example.aicrmdash.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.*

/**
 * REST controller for authentication endpoints.
 *
 * Handles user login and registration.
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request Login credentials
     * @return LoginResponse with JWT token and user details
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        return try {
            val response = authService.login(request)
            ResponseEntity.ok(response)
        } catch (e: BadCredentialsException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    /**
     * Registers a new user in the system.
     *
     * @param request Registration details
     * @return LoginResponse with JWT token and user details
     */
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<Any> {
        return try {
            val response = authService.register(request)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to (e.message ?: "Registration failed")))
        }
    }
}
