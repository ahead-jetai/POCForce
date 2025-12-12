package com.example.aicrmdash.controller

import com.example.aicrmdash.domain.User
import com.example.aicrmdash.domain.UserRole
import com.example.aicrmdash.dto.LoginRequest
import com.example.aicrmdash.dto.RegisterRequest
import com.example.aicrmdash.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * Integration tests for AuthController endpoints.
 *
 * Tests user authentication (login and registration) functionality.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
    }

    @Test
    fun `should register new user successfully`() {
        val request = RegisterRequest(
            name = "John Doe",
            email = "john@example.com",
            password = "password123",
            role = "SE"
        )

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.user.id").exists())
            .andExpect(jsonPath("$.user.email").value("john@example.com"))
            .andExpect(jsonPath("$.user.name").value("John Doe"))
            .andExpect(jsonPath("$.user.role").value("SE"))
    }

    @Test
    fun `should reject registration with duplicate email`() {
        // Create existing user
        val existingUser = User(
            name = "Existing User",
            email = "john@example.com",
            password = passwordEncoder.encode("password123"),
            role = UserRole.SE
        )
        userRepository.save(existingUser)

        val request = RegisterRequest(
            name = "John Doe",
            email = "john@example.com",
            password = "password123",
            role = "SE"
        )

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Email already registered"))
    }

    @Test
    fun `should reject registration with invalid role`() {
        val request = RegisterRequest(
            name = "John Doe",
            email = "john@example.com",
            password = "password123",
            role = "INVALID_ROLE"
        )

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").exists())
    }

    @Test
    fun `should reject registration with missing fields`() {
        val request = mapOf(
            "email" to "john@example.com"
            // Missing name, password, and role
        )

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should login successfully with valid credentials`() {
        // Create test user
        val testUser = User(
            name = "Test User",
            email = "test@example.com",
            password = passwordEncoder.encode("password123"),
            role = UserRole.SE
        )
        userRepository.save(testUser)

        val request = LoginRequest(
            email = "test@example.com",
            password = "password123"
        )

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.user.id").exists())
            .andExpect(jsonPath("$.user.email").value("test@example.com"))
            .andExpect(jsonPath("$.user.name").value("Test User"))
            .andExpect(jsonPath("$.user.role").value("SE"))
    }

    @Test
    fun `should reject login with invalid email`() {
        val request = LoginRequest(
            email = "nonexistent@example.com",
            password = "password123"
        )

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `should reject login with invalid password`() {
        // Create test user
        val testUser = User(
            name = "Test User",
            email = "test@example.com",
            password = passwordEncoder.encode("password123"),
            role = UserRole.SE
        )
        userRepository.save(testUser)

        val request = LoginRequest(
            email = "test@example.com",
            password = "wrongpassword"
        )

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `should reject login with missing fields`() {
        val request = mapOf(
            "email" to "test@example.com"
            // Missing password
        )

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should reject login with invalid email format`() {
        val request = LoginRequest(
            email = "invalid-email",
            password = "password123"
        )

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should allow access to secured endpoint with valid token`() {
        // Create test user
        val testUser = User(
            name = "Test User",
            email = "test@example.com",
            password = passwordEncoder.encode("password123"),
            role = UserRole.SE
        )
        userRepository.save(testUser)

        // Login to get token
        val loginRequest = LoginRequest(
            email = "test@example.com",
            password = "password123"
        )

        val loginResult = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        val loginResponse = objectMapper.readTree(loginResult.response.contentAsString)
        val token = loginResponse.get("token").asText()

        // Try to access secured endpoint with token
        mockMvc.perform(
            get("/api/v1/users")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `should reject access to secured endpoint without token`() {
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `should reject access to secured endpoint with invalid token`() {
        mockMvc.perform(
            get("/api/v1/users")
                .header("Authorization", "Bearer invalid.token.here")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `should allow access to health endpoint without authentication`() {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk)
    }

    @Test
    fun `should allow access to swagger UI without authentication`() {
        mockMvc.perform(get("/swagger-ui/index.html"))
            .andExpect(status().isOk)
    }
}
