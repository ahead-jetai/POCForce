package com.example.aicrmdash.controller

import com.example.aicrmdash.config.TestSecurityConfig
import com.example.aicrmdash.dto.CreatePocRequest
import com.example.aicrmdash.dto.UpdateRequirementRequest
import com.example.aicrmdash.repository.PocRepository
import com.example.aicrmdash.repository.RequirementRepository
import com.example.aicrmdash.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Integration tests for RequirementController.
 *
 * Tests all REST endpoints for requirement management including listing
 * and updating requirements for a POC.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig::class)
class RequirementControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var pocRepository: PocRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var requirementRepository: RequirementRepository

    private var testUserId: Long = 0

    @BeforeEach
    fun setup() {
        // Clean up database before each test
        pocRepository.deleteAll()
        requirementRepository.deleteAll()

        // Get a test user (assumes users are seeded)
        val users = userRepository.findAll()
        testUserId = users.firstOrNull()?.id ?: 1L
    }

    // ========== LIST REQUIREMENTS TESTS ==========

    @Test
    fun `GET should return all requirements for POC`() {
        val pocId = createTestPoc("Acme Corp", "Test POC")

        mockMvc.perform(get("/api/v1/pocs/$pocId/requirements"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").isNotEmpty)
            .andExpect(jsonPath("$[0].pocId").value(pocId))
            .andExpect(jsonPath("$[0].description").exists())
            .andExpect(jsonPath("$[0].completed").exists())
            .andExpect(jsonPath("$[0].phase").value("DISCOVERY"))
    }

    @Test
    fun `GET should return 404 when POC does not exist`() {
        mockMvc.perform(get("/api/v1/pocs/99999/requirements"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `GET should return empty list when POC has no requirements for current phase`() {
        val pocId = createTestPoc("Acme Corp", "Test POC")
        
        // Delete all requirements
        val requirements = requirementRepository.findAll().filter { it.pocId == pocId }
        requirementRepository.deleteAll(requirements)

        mockMvc.perform(get("/api/v1/pocs/$pocId/requirements"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    // ========== UPDATE REQUIREMENT TESTS ==========

    @Test
    fun `PATCH should mark requirement as complete`() {
        val pocId = createTestPoc("Acme Corp", "Test POC")
        val requirements = requirementRepository.findAll().filter { it.pocId == pocId }
        val requirementId = requirements.first().id

        val updateRequest = UpdateRequirementRequest(
            completed = true,
            notes = null
        )

        mockMvc.perform(
            patch("/api/v1/pocs/$pocId/requirements/$requirementId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(requirementId))
            .andExpect(jsonPath("$.completed").value(true))
            .andExpect(jsonPath("$.completedAt").exists())
    }

    @Test
    fun `PATCH should mark requirement as incomplete`() {
        val pocId = createTestPoc("Acme Corp", "Test POC")
        val requirements = requirementRepository.findAll().filter { it.pocId == pocId }
        val requirementId = requirements.first().id

        // First mark as complete
        val completeRequest = UpdateRequirementRequest(completed = true, notes = null)
        mockMvc.perform(
            patch("/api/v1/pocs/$pocId/requirements/$requirementId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completeRequest))
        )

        // Then mark as incomplete
        val incompleteRequest = UpdateRequirementRequest(completed = false, notes = null)
        mockMvc.perform(
            patch("/api/v1/pocs/$pocId/requirements/$requirementId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(requirementId))
            .andExpect(jsonPath("$.completed").value(false))
            .andExpect(jsonPath("$.completedAt").isEmpty)
    }

    @Test
    fun `PATCH should update requirement with notes`() {
        val pocId = createTestPoc("Acme Corp", "Test POC")
        val requirements = requirementRepository.findAll().filter { it.pocId == pocId }
        val requirementId = requirements.first().id

        val updateRequest = UpdateRequirementRequest(
            completed = true,
            notes = "Completed successfully with additional testing"
        )

        mockMvc.perform(
            patch("/api/v1/pocs/$pocId/requirements/$requirementId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(requirementId))
            .andExpect(jsonPath("$.completed").value(true))
            .andExpect(jsonPath("$.notes").value("Completed successfully with additional testing"))
    }

    @Test
    fun `PATCH should return 404 when POC does not exist`() {
        val updateRequest = UpdateRequirementRequest(completed = true, notes = null)

        mockMvc.perform(
            patch("/api/v1/pocs/99999/requirements/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `PATCH should return 404 when requirement does not exist`() {
        val pocId = createTestPoc("Acme Corp", "Test POC")
        val updateRequest = UpdateRequirementRequest(completed = true, notes = null)

        mockMvc.perform(
            patch("/api/v1/pocs/$pocId/requirements/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `PATCH should return 400 when notes exceed max length`() {
        val pocId = createTestPoc("Acme Corp", "Test POC")
        val requirements = requirementRepository.findAll().filter { it.pocId == pocId }
        val requirementId = requirements.first().id

        val updateRequest = UpdateRequirementRequest(
            completed = true,
            notes = "a".repeat(1001) // Exceeds 1000 character limit
        )

        mockMvc.perform(
            patch("/api/v1/pocs/$pocId/requirements/$requirementId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `PATCH should update only notes when completed is null`() {
        val pocId = createTestPoc("Acme Corp", "Test POC")
        val requirements = requirementRepository.findAll().filter { it.pocId == pocId }
        val requirementId = requirements.first().id

        val updateRequest = UpdateRequirementRequest(
            completed = null,
            notes = "Added some notes without changing completion status"
        )

        mockMvc.perform(
            patch("/api/v1/pocs/$pocId/requirements/$requirementId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(requirementId))
            .andExpect(jsonPath("$.completed").value(false)) // Should remain unchanged
            .andExpect(jsonPath("$.notes").value("Added some notes without changing completion status"))
    }

    // ========== HELPER METHODS ==========

    private fun createTestPoc(customerName: String, title: String): Long {
        val request = CreatePocRequest(
            customerName = customerName,
            title = title,
            description = "Test POC description",
            dealValue = BigDecimal("50000.00"),
            projectedCloseDate = LocalDate.now().plusMonths(3),
            kickoffDate = LocalDate.now(),
            endDate = LocalDate.now().plusMonths(2),
            ownerId = testUserId
        )

        val result = mockMvc.perform(
            post("/api/v1/pocs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val responseBody = result.response.contentAsString
        val jsonNode = objectMapper.readTree(responseBody)
        return jsonNode.get("id").asLong()
    }
}
