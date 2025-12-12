package com.example.aicrmdash.controller

import com.example.aicrmdash.config.TestSecurityConfig
import com.example.aicrmdash.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * Integration tests for UserController.
 *
 * Tests all REST endpoints for user management including listing
 * all users and retrieving individual users.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig::class)
class UserControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    // ========== LIST USERS TESTS ==========

    @Test
    fun `GET should return all users`() {
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").isNotEmpty)
            .andExpect(jsonPath("$[0].id").exists())
            .andExpect(jsonPath("$[0].name").exists())
            .andExpect(jsonPath("$[0].email").exists())
            .andExpect(jsonPath("$[0].role").exists())
    }

    @Test
    fun `GET should return users with correct structure`() {
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").isNumber())
            .andExpect(jsonPath("$[0].name").isString())
            .andExpect(jsonPath("$[0].email").isString())
            .andExpect(jsonPath("$[0].role").isString())
    }

    // ========== GET SINGLE USER TESTS ==========

    @Test
    fun `GET by ID should return user when exists`() {
        val users = userRepository.findAll()
        val userId = users.first().id

        mockMvc.perform(get("/api/v1/users/$userId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.name").exists())
            .andExpect(jsonPath("$.email").exists())
            .andExpect(jsonPath("$.role").exists())
    }

    @Test
    fun `GET by ID should return 404 when user does not exist`() {
        mockMvc.perform(get("/api/v1/users/99999"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `GET by ID should return user with expected role values`() {
        val users = userRepository.findAll()
        val userId = users.first().id

        mockMvc.perform(get("/api/v1/users/$userId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.role").value(
                org.hamcrest.Matchers.isOneOf("SE", "CSE", "SALES_ENGINEER", "MANAGER")
            ))
    }

    @Test
    fun `GET by ID should return user with valid email format`() {
        val users = userRepository.findAll()
        val userId = users.first().id

        mockMvc.perform(get("/api/v1/users/$userId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value(
                org.hamcrest.Matchers.containsString("@")
            ))
    }

    @Test
    fun `GET should return at least seed users`() {
        // Based on T-8, we expect at least 3 seed users
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)))
    }

    @Test
    fun `GET by ID should return correct user data`() {
        val users = userRepository.findAll()
        val user = users.first()

        mockMvc.perform(get("/api/v1/users/${user.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(user.id))
            .andExpect(jsonPath("$.name").value(user.name))
            .andExpect(jsonPath("$.email").value(user.email))
            .andExpect(jsonPath("$.role").value(user.role.name))
    }
}
