package com.example.aicrmdash.controller

import com.example.aicrmdash.dto.ClosePocRequest
import com.example.aicrmdash.dto.CreatePocRequest
import com.example.aicrmdash.dto.UpdatePocRequest
import com.example.aicrmdash.repository.PocRepository
import com.example.aicrmdash.repository.RequirementRepository
import com.example.aicrmdash.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Integration tests for PocController.
 *
 * Tests all REST endpoints for POC management including CRUD operations,
 * phase transitions, and closure.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class PocControllerIntegrationTest {

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

    @Autowired
    private lateinit var entityManager: EntityManager

    private val baseUrl = "/api/v1/pocs"
    private var testUserId: Long = 0

    @BeforeEach
    fun setup() {
        // Clean up database before each test
        // Delete POCs first (cascade will handle requirements)
        pocRepository.deleteAll()
        requirementRepository.deleteAll()

        // Get a test user (assumes users are seeded)
        val users = userRepository.findAll()
        testUserId = users.firstOrNull()?.id ?: 1L
    }

    // ========== CREATE POC TESTS ==========

    @Test
    fun `POST should create POC with valid data`() {
        val request = CreatePocRequest(
            customerName = "Acme Corp",
            title = "Cloud Migration POC",
            description = "Migrate legacy systems to cloud",
            dealValue = BigDecimal("50000.00"),
            projectedCloseDate = LocalDate.now().plusMonths(3),
            kickoffDate = LocalDate.now(),
            endDate = LocalDate.now().plusMonths(2),
            ownerId = testUserId
        )

        mockMvc.perform(
            post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.customerName").value("Acme Corp"))
            .andExpect(jsonPath("$.title").value("Cloud Migration POC"))
            .andExpect(jsonPath("$.currentPhase").value("DISCOVERY"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.ownerId").value(testUserId))
    }

    @Test
    fun `POST should return 400 when customerName is blank`() {
        val request = CreatePocRequest(
            customerName = "",
            title = "Cloud Migration POC",
            description = "Migrate legacy systems to cloud",
            dealValue = BigDecimal("50000.00"),
            projectedCloseDate = LocalDate.now().plusMonths(3),
            kickoffDate = LocalDate.now(),
            endDate = LocalDate.now().plusMonths(2),
            ownerId = testUserId
        )

        mockMvc.perform(
            post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST should return 400 when dealValue is negative`() {
        val request = CreatePocRequest(
            customerName = "Acme Corp",
            title = "Cloud Migration POC",
            description = "Migrate legacy systems to cloud",
            dealValue = BigDecimal("-1000.00"),
            projectedCloseDate = LocalDate.now().plusMonths(3),
            kickoffDate = LocalDate.now(),
            endDate = LocalDate.now().plusMonths(2),
            ownerId = testUserId
        )

        mockMvc.perform(
            post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST should return 400 when endDate is before kickoffDate`() {
        val request = CreatePocRequest(
            customerName = "Acme Corp",
            title = "Cloud Migration POC",
            description = "Migrate legacy systems to cloud",
            dealValue = BigDecimal("50000.00"),
            projectedCloseDate = LocalDate.now().plusMonths(3),
            kickoffDate = LocalDate.now(),
            endDate = LocalDate.now().minusDays(1),
            ownerId = testUserId
        )

        mockMvc.perform(
            post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    // ========== LIST POC TESTS ==========

    @Test
    fun `GET should return all POCs`() {
        // Create test POCs
        createTestPoc("Customer A", "POC A")
        createTestPoc("Customer B", "POC B")

        mockMvc.perform(get(baseUrl))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].customerName").exists())
            .andExpect(jsonPath("$[0].title").exists())
    }

    @Test
    fun `GET should return empty list when no POCs exist`() {
        mockMvc.perform(get(baseUrl))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `GET should filter POCs by phase`() {
        // Create POCs in different phases
        val poc1Id = createTestPoc("Customer A", "POC A")
        val poc2Id = createTestPoc("Customer B", "POC B")
        
        // Advance poc2 to PLANNING phase
        advanceToPhase(poc2Id)

        mockMvc.perform(get("$baseUrl?phase=DISCOVERY"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].currentPhase").value("DISCOVERY"))
    }

    @Test
    fun `GET should filter POCs by ownerId`() {
        createTestPoc("Customer A", "POC A")

        mockMvc.perform(get("$baseUrl?ownerId=$testUserId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].ownerId").value(testUserId))
    }

    @Test
    fun `GET should search POCs by customer name`() {
        createTestPoc("Acme Corporation", "POC A")
        createTestPoc("Beta Industries", "POC B")

        mockMvc.perform(get("$baseUrl?search=Acme"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].customerName").value("Acme Corporation"))
    }

    @Test
    fun `GET should search POCs by title`() {
        createTestPoc("Customer A", "Cloud Migration")
        createTestPoc("Customer B", "Data Analytics")

        mockMvc.perform(get("$baseUrl?search=Cloud"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].title").value("Cloud Migration"))
    }

    // ========== GET SINGLE POC TESTS ==========

    @Test
    fun `GET by ID should return POC when exists`() {
        val pocId = createTestPoc("Acme Corp", "Cloud POC")

        mockMvc.perform(get("$baseUrl/$pocId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(pocId))
            .andExpect(jsonPath("$.customerName").value("Acme Corp"))
            .andExpect(jsonPath("$.title").value("Cloud POC"))
            .andExpect(jsonPath("$.currentPhase").value("DISCOVERY"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
    }

    @Test
    fun `GET by ID should return 404 when POC does not exist`() {
        mockMvc.perform(get("$baseUrl/99999"))
            .andExpect(status().isNotFound)
    }

    // ========== UPDATE POC TESTS ==========

    @Test
    fun `PUT should update POC with valid data`() {
        val pocId = createTestPoc("Acme Corp", "Cloud POC")

        val updateRequest = UpdatePocRequest(
            customerName = "Updated Corp",
            title = "Updated Title",
            description = "Updated description",
            dealValue = BigDecimal("75000.00"),
            projectedCloseDate = LocalDate.now().plusMonths(4),
            kickoffDate = LocalDate.now(),
            endDate = LocalDate.now().plusMonths(3),
            ownerId = testUserId
        )

        mockMvc.perform(
            put("$baseUrl/$pocId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(pocId))
            .andExpect(jsonPath("$.customerName").value("Updated Corp"))
            .andExpect(jsonPath("$.title").value("Updated Title"))
            .andExpect(jsonPath("$.dealValue").value(75000.00))
    }

    @Test
    fun `PUT should return 404 when POC does not exist`() {
        val updateRequest = UpdatePocRequest(
            customerName = "Updated Corp",
            title = "Updated Title",
            description = "Updated description",
            dealValue = BigDecimal("75000.00"),
            projectedCloseDate = LocalDate.now().plusMonths(4),
            kickoffDate = LocalDate.now(),
            endDate = LocalDate.now().plusMonths(3),
            ownerId = testUserId
        )

        mockMvc.perform(
            put("$baseUrl/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `PUT should return 400 when updating terminal phase POC`() {
        val pocId = createTestPoc("Acme Corp", "Cloud POC")
        
        // Close the POC
        closePoc(pocId, "WON")

        val updateRequest = UpdatePocRequest(
            customerName = "Updated Corp",
            title = "Updated Title",
            description = "Updated description",
            dealValue = BigDecimal("75000.00"),
            projectedCloseDate = LocalDate.now().plusMonths(4),
            kickoffDate = LocalDate.now(),
            endDate = LocalDate.now().plusMonths(3),
            ownerId = testUserId
        )

        mockMvc.perform(
            put("$baseUrl/$pocId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `PUT should return 400 when validation fails`() {
        val pocId = createTestPoc("Acme Corp", "Cloud POC")

        val updateRequest = UpdatePocRequest(
            customerName = "",  // Invalid: blank
            title = "Updated Title",
            description = "Updated description",
            dealValue = BigDecimal("75000.00"),
            projectedCloseDate = LocalDate.now().plusMonths(4),
            kickoffDate = LocalDate.now(),
            endDate = LocalDate.now().plusMonths(3),
            ownerId = testUserId
        )

        mockMvc.perform(
            put("$baseUrl/$pocId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isBadRequest)
    }

    // ========== DELETE POC TESTS ==========

    @Test
    fun deleteShouldRemovePocWhenExists() {
        val pocId = createTestPoc("Acme Corp", "Cloud POC")

        mockMvc.perform(delete("$baseUrl/$pocId"))
            .andDo { println("Response: ${it.response.contentAsString}") }
            .andExpect(status().isNoContent)
    }

    @Test
    fun `DELETE should return 404 when POC does not exist`() {
        mockMvc.perform(delete("$baseUrl/99999"))
            .andExpect(status().isNotFound)
    }

    // ========== ADVANCE PHASE TESTS ==========

    @Test
    fun `POST advance should advance phase when all requirements complete`() {
        val pocId = createTestPoc("Acme Corp", "Cloud POC")
        
        // Complete all requirements for current phase
        completeAllRequirements(pocId)

        mockMvc.perform(post("$baseUrl/$pocId/advance"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(pocId))
            .andExpect(jsonPath("$.currentPhase").value("PLANNING"))
    }

    @Test
    fun `POST advance should return 400 when requirements incomplete`() {
        val pocId = createTestPoc("Acme Corp", "Cloud POC")
        
        // Don't complete requirements

        mockMvc.perform(post("$baseUrl/$pocId/advance"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST advance should return 404 when POC does not exist`() {
        mockMvc.perform(post("$baseUrl/99999/advance"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `POST advance should return 400 when in terminal phase`() {
        val pocId = createTestPoc("Acme Corp", "Cloud POC")
        
        // Close the POC
        closePoc(pocId, "WON")

        mockMvc.perform(post("$baseUrl/$pocId/advance"))
            .andExpect(status().isBadRequest)
    }

    // ========== CLOSE POC TESTS ==========

    @Test
    fun `POST close should close POC as WON`() {
        val pocId = createTestPoc("Acme Corp", "Cloud POC")

        val closeRequest = ClosePocRequest(
            outcome = "WON",
            notes = "Successfully closed the deal"
        )

        mockMvc.perform(
            post("$baseUrl/$pocId/close")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(closeRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(pocId))
            .andExpect(jsonPath("$.currentPhase").value("CLOSED_WON"))
            .andExpect(jsonPath("$.status").value("CLOSED"))
    }

    @Test
    fun `POST close should close POC as LOST`() {
        val pocId = createTestPoc("Acme Corp", "Cloud POC")

        val closeRequest = ClosePocRequest(
            outcome = "LOST",
            notes = "Customer chose competitor"
        )

        mockMvc.perform(
            post("$baseUrl/$pocId/close")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(closeRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(pocId))
            .andExpect(jsonPath("$.currentPhase").value("CLOSED_LOST"))
            .andExpect(jsonPath("$.status").value("CLOSED"))
    }

    @Test
    fun `POST close should return 400 with invalid outcome`() {
        val pocId = createTestPoc("Acme Corp", "Cloud POC")

        val closeRequest = ClosePocRequest(
            outcome = "INVALID",
            notes = "Test"
        )

        mockMvc.perform(
            post("$baseUrl/$pocId/close")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(closeRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST close should return 404 when POC does not exist`() {
        val closeRequest = ClosePocRequest(
            outcome = "WON",
            notes = "Test"
        )

        mockMvc.perform(
            post("$baseUrl/99999/close")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(closeRequest))
        )
            .andExpect(status().isNotFound)
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
            post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val responseBody = result.response.contentAsString
        val jsonNode = objectMapper.readTree(responseBody)
        return jsonNode.get("id").asLong()
    }

    private fun completeAllRequirements(pocId: Long) {
        val poc = pocRepository.findById(pocId).orElseThrow()
        val requirements = requirementRepository.findAll()
            .filter { it.pocId == pocId && it.phase == poc.currentPhase }
        
        requirements.forEach { requirement ->
            mockMvc.perform(
                patch("/api/v1/pocs/$pocId/requirements/${requirement.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"completed": true}""")
            )
        }
    }

    private fun advanceToPhase(pocId: Long) {
        completeAllRequirements(pocId)
        mockMvc.perform(post("$baseUrl/$pocId/advance"))
    }

    private fun closePoc(pocId: Long, outcome: String) {
        val closeRequest = ClosePocRequest(
            outcome = outcome,
            notes = "Closing POC"
        )

        mockMvc.perform(
            post("$baseUrl/$pocId/close")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(closeRequest))
        )
    }
}
