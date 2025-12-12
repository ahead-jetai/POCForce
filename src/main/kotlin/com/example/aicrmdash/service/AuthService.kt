package com.example.aicrmdash.service

import com.example.aicrmdash.domain.User
import com.example.aicrmdash.domain.UserRole
import com.example.aicrmdash.dto.LoginRequest
import com.example.aicrmdash.dto.LoginResponse
import com.example.aicrmdash.dto.RegisterRequest
import com.example.aicrmdash.exception.ResourceNotFoundException
import com.example.aicrmdash.repository.UserRepository
import com.example.aicrmdash.security.JwtUtil
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/**
 * Service for handling user authentication and registration.
 */
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request Login credentials
     * @return LoginResponse with JWT token and user details
     * @throws BadCredentialsException if credentials are invalid
     */
    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw BadCredentialsException("Invalid email or password")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw BadCredentialsException("Invalid email or password")
        }

        val token = jwtUtil.generateToken(user.email)

        return LoginResponse(
            token = token,
            user = com.example.aicrmdash.dto.UserDto(
                id = user.id,
                name = user.name,
                email = user.email,
                role = user.role.name
            )
        )
    }

    /**
     * Registers a new user in the system.
     *
     * @param request Registration details
     * @return LoginResponse with JWT token and user details
     * @throws IllegalArgumentException if email already exists or role is invalid
     */
    fun register(request: RegisterRequest): LoginResponse {
        if (userRepository.findByEmail(request.email) != null) {
            throw IllegalArgumentException("Email already registered")
        }

        val role = try {
            UserRole.valueOf(request.role.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid role: ${request.role}")
        }

        val user = User(
            name = request.name,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            role = role
        )

        val savedUser = userRepository.save(user)
        val token = jwtUtil.generateToken(savedUser.email)

        return LoginResponse(
            token = token,
            user = com.example.aicrmdash.dto.UserDto(
                id = savedUser.id,
                name = savedUser.name,
                email = savedUser.email,
                role = savedUser.role.name
            )
        )
    }
}
