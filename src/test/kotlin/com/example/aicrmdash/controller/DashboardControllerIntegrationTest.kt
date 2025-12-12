package com.example.aicrmdash.controller

import com.example.aicrmdash.dto.CreatePocRequest
import com.example.aicrmdash.repository.PocRepository
import com.example.aicrmdash.repository.RequirementRepository
import com.example.aicrmdash.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
 * Integration tests for DashboardController.
 *
 * Tests the dashboard summary endpoint with various POC states
 * to verify correct calculation of metrics.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerIntegrationTest {

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

    // ========== DASHBOARD SUMMARY TESTS ==========

    @Test
    fun `GET summary should return correct structure`() {
        mockMvc.perform(get("/api/v1/dashboard/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.activePocCount").exists())
            .andExpect(jsonPath("$.atRiskPocCount").exists())
            .andExpect(jsonPath("$.totalPipelineValue").exists())
            .andExpect(jsonPath("$.pocsByPhase").exists())
    }

    @Test
    fun `GET summary should return zero counts when no POCs exist`() {
        mockMvc.perform(get("/api/v1/dashboard/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.activePocCount").value(0))
            .andExpect(jsonPath("$.atRiskPocCount").value(0))
            .andExpect(jsonPath("$.totalPipelineValue").value(0))
    }

    @Test
    fun `GET summary should count active POCs correctly`() {
        // Create 3 active POCs
        createTestPoc("Customer A", "POC A", BigDecimal("10000"))
        createTestPoc("Customer B", "POC B", BigDecimal("20000"))
        createTestPoc("Customer C", "POC C", BigDecimal("30000"))

        mockMvc.perform(get("/api/v1/dashboard/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.activePocCount").value(3))
            .andExpect(jsonPath("$.totalPipelineValue").value(60000.00))
    }

    @Test
    fun `GET summary should count at-risk POCs correctly`() {
        // Create POCs with endDate in the past (will be AT_RISK)
        createTestPocWithDates(
            "Customer A", "POC A", BigDecimal("10000"),
            LocalDate.now().minusMonths(2), // kickoff in past
            LocalDate.now().minusDays(1)    // end date in past (AT_RISK)
        )
        createTestPocWithDates(
            "Customer B", "POC B", BigDecimal("20000"),
            LocalDate.now().minusMonths(1),
            LocalDate.now().minusDays(1)    // end date in past (AT_RISK)
        )

        mockMvc.perform(get("/api/v1/dashboard/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.atRiskPocCount").value(2))
            .andExpect(jsonPath("$.activePocCount").value(2)) // Both are still active (not closed)
    }

    @Test
    fun `GET summary should count POCs by phase correctly`() {
        // Create POCs in DISCOVERY phase
        val poc1Id = createTestPoc("Customer A", "POC A", BigDecimal("10000"))
        val poc2Id = createTestPoc("Customer B", "POC B", BigDecimal("20000"))
        
        // Advance one POC to PLANNING phase
        completeAllRequirements(poc1Id)
        mockMvc.perform(post("/api/v1/pocs/$poc1Id/advance"))

        mockMvc.perform(get("/api/v1/dashboard/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.pocsByPhase.DISCOVERY").value(1))
            .andExpect(jsonPath("$.pocsByPhase.PLANNING").value(1))
    }

    @Test
    fun `GET summary should exclude closed POCs from active count`() {
        // Create POCs
        val poc1Id = createTestPoc("Customer A", "POC A", BigDecimal("10000"))
        val poc2Id = createTestPoc("Customer B", "POC B", BigDecimal("20000"))
        val poc3Id = createTestPoc("Customer C", "POC C", BigDecimal("30000"))

        // Close one POC
        closePoc(poc1Id, "WON")

        mockMvc.perform(get("/api/v1/dashboard/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.activePocCount").value(2))
            .andExpect(jsonPath("$.totalPipelineValue").value(50000.00)) // Only active POCs
    }

    @Test
    fun `GET summary should count closed POCs by phase`() {
        val poc1Id = createTestPoc("Customer A", "POC A", BigDecimal("10000"))
        val poc2Id = createTestPoc("Customer B", "POC B", BigDecimal("20000"))

        closePoc(poc1Id, "WON")
        closePoc(poc2Id, "LOST")

        mockMvc.perform(get("/api/v1/dashboard/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.pocsByPhase.CLOSED_WON").value(1))
            .andExpect(jsonPath("$.pocsByPhase.CLOSED_LOST").value(1))
            .andExpect(jsonPath("$.activePocCount").value(0))
    }

    @Test
    fun `GET summary should calculate pipeline value correctly`() {
        createTestPoc("Customer A", "POC A", BigDecimal("15000.50"))
        createTestPoc("Customer B", "POC B", BigDecimal("25000.75"))
        createTestPoc("Customer C", "POC C", BigDecimal("10000.25"))

        mockMvc.perform(get("/api/v1/dashboard/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalPipelineValue").value(50001.50))
    }

    @Test
    fun `GET summary should handle multiple phases correctly`() {
        // Create POCs in different phases
        val poc1Id = createTestPoc("Customer A", "POC A", BigDecimal("10000"))
        val poc2Id = createTestPoc("Customer B", "POC B", BigDecimal("20000"))
        val poc3Id = createTestPoc("Customer C", "POC C", BigDecimal("30000"))

        // Advance POCs to different phases
        completeAllRequirements(poc1Id)
        mockMvc.perform(post("/api/v1/pocs/$poc1Id/advance")) // PLANNING

        completeAllRequirements(poc2Id)
        mockMvc.perform(post("/api/v1/pocs/$poc2Id/advance")) // PLANNING
        completeAllRequirements(poc2Id)
        mockMvc.perform(post("/api/v1/pocs/$poc2Id/advance")) // EXECUTION

        mockMvc.perform(get("/api/v1/dashboard/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.pocsByPhase.DISCOVERY").value(1))
            .andExpect(jsonPath("$.pocsByPhase.PLANNING").value(1))
            .andExpect(jsonPath("$.pocsByPhase.EXECUTION").value(1))
            .andExpect(jsonPath("$.activePocCount").value(3))
    }

    // ========== HELPER METHODS ==========

    private fun createTestPoc(customerName: String, title: String, dealValue: BigDecimal): Long {
        return createTestPocWithDates(
            customerName, title, dealValue,
            LocalDate.now(),
            LocalDate.now().plusMonths(2)
        )
    }

    private fun createTestPocWithDates(
        customerName: String,
        title: String,
        dealValue: BigDecimal,
        kickoffDate: LocalDate,
        endDate: LocalDate
    ): Long {
        val request = CreatePocRequest(
            customerName = customerName,
            title = title,
            description = "Test POC description",
            dealValue = dealValue,
            projectedCloseDate = LocalDate.now().plusMonths(3),
            kickoffDate = kickoffDate,
            endDate = endDate,
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

    private fun closePoc(pocId: Long, outcome: String) {
        val closeRequest = mapOf(
            "outcome" to outcome,
            "notes" to "Test closure"
        )

        mockMvc.perform(
            post("/api/v1/pocs/$pocId/close")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(closeRequest))
        )
    }
}
