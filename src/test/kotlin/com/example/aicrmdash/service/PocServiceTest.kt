package com.example.aicrmdash.service

import com.example.aicrmdash.config.DefaultRequirements
import com.example.aicrmdash.domain.Phase
import com.example.aicrmdash.domain.Poc
import com.example.aicrmdash.domain.Requirement
import com.example.aicrmdash.domain.Status
import com.example.aicrmdash.domain.User
import com.example.aicrmdash.domain.UserRole
import com.example.aicrmdash.dto.CreatePocRequest
import com.example.aicrmdash.dto.UpdatePocRequest
import com.example.aicrmdash.exception.PhaseTransitionException
import com.example.aicrmdash.exception.ResourceNotFoundException
import com.example.aicrmdash.exception.ValidationException
import com.example.aicrmdash.repository.PhaseTransitionRepository
import com.example.aicrmdash.repository.PocRepository
import com.example.aicrmdash.repository.RequirementRepository
import com.example.aicrmdash.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Unit tests for PocService.
 *
 * Tests all business logic for POC management including CRUD operations,
 * status calculation, filtering, phase transitions, and closure.
 */
@ExtendWith(MockitoExtension::class)
class PocServiceTest {

    @Mock
    private lateinit var pocRepository: PocRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var requirementRepository: RequirementRepository

    @Mock
    private lateinit var phaseTransitionRepository: PhaseTransitionRepository

    @InjectMocks
    private lateinit var pocService: PocService

    // Test data
    private val testOwnerId = 1L
    private val testPocId = 100L
    private val testCustomerName = "Acme Corp"
    private val testTitle = "Cloud Migration POC"
    private val testDescription = "Migrate legacy systems to cloud"
    private val testDealValue = BigDecimal("50000.00")
    private val testProjectedCloseDate = LocalDate.now().plusMonths(3)
    private val testKickoffDate = LocalDate.now()
    private val testEndDate = LocalDate.now().plusMonths(2)

    private fun createTestPoc(
        id: Long = testPocId,
        phase: Phase = Phase.DISCOVERY,
        status: Status = Status.ACTIVE,
        endDate: LocalDate = testEndDate,
        kickoffDate: LocalDate = testKickoffDate
    ): Poc {
        return Poc(
            id = id,
            customerName = testCustomerName,
            title = testTitle,
            description = testDescription,
            dealValue = testDealValue,
            projectedCloseDate = testProjectedCloseDate,
            kickoffDate = kickoffDate,
            endDate = endDate,
            currentPhase = phase,
            ownerId = testOwnerId,
            status = status,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun createTestRequest(): CreatePocRequest {
        return CreatePocRequest(
            customerName = testCustomerName,
            title = testTitle,
            description = testDescription,
            dealValue = testDealValue,
            projectedCloseDate = testProjectedCloseDate,
            kickoffDate = testKickoffDate,
            endDate = testEndDate,
            ownerId = testOwnerId
        )
    }

    private fun createUpdateRequest(): UpdatePocRequest {
        return UpdatePocRequest(
            customerName = "Updated Corp",
            title = "Updated Title",
            description = "Updated description",
            dealValue = BigDecimal("75000.00"),
            projectedCloseDate = testProjectedCloseDate.plusMonths(1),
            kickoffDate = testKickoffDate,
            endDate = testEndDate.plusMonths(1),
            ownerId = testOwnerId
        )
    }

    private fun createTestUser(id: Long = testOwnerId): User {
        return User(
            id = id,
            name = "Test User",
            email = "test@example.com",
            role = UserRole.SE,
            password = "password123"
        )
    }

    // ========== CREATE POC TESTS ==========

    @Test
    fun `createPoc should create POC with valid data and generate requirements`() {
        // Given
        val request = createTestRequest()
        val savedPoc = createTestPoc()
        val testUser = createTestUser()

        doReturn(true).whenever(userRepository).existsById(testOwnerId)
        doReturn(Optional.of(testUser)).whenever(userRepository).findById(testOwnerId)
        doReturn(savedPoc).whenever(pocRepository).save(any())

        // When
        val result = pocService.createPoc(request)

        // Then
        assertNotNull(result)
        assertEquals(Phase.DISCOVERY, result.currentPhase)
        assertEquals(Status.ACTIVE, result.status)
        verify(userRepository).existsById(testOwnerId)
        verify(pocRepository).save(any())
    }

    @Test
    fun `createPoc should throw ValidationException when owner does not exist`() {
        // Given
        val request = createTestRequest()
        whenever(userRepository.existsById(testOwnerId)).thenReturn(false)

        // When & Then
        val exception = assertThrows(ValidationException::class.java) {
            pocService.createPoc(request)
        }
        assertTrue(exception.message!!.contains("Owner with ID"))
        verify(pocRepository, never()).save(any())
    }

    @Test
    fun `createPoc should throw ValidationException when endDate is before kickoffDate`() {
        // Given
        val request = createTestRequest().copy(
            kickoffDate = LocalDate.now(),
            endDate = LocalDate.now().minusDays(1)
        )
        whenever(userRepository.existsById(testOwnerId)).thenReturn(true)

        // When & Then
        val exception = assertThrows(ValidationException::class.java) {
            pocService.createPoc(request)
        }
        assertTrue(exception.message!!.contains("End date must be on or after kickoff date"))
        verify(pocRepository, never()).save(any())
    }

    // ========== GET POC TESTS ==========

    @Test
    fun `getAllPocs should return all POCs`() {
        // Given
        val pocs = listOf(
            createTestPoc(id = 1),
            createTestPoc(id = 2),
            createTestPoc(id = 3)
        )
        whenever(pocRepository.findAll()).thenReturn(pocs)

        // When
        val result = pocService.getAllPocs()

        // Then
        assertEquals(3, result.size)
        verify(pocRepository).findAll()
    }

    @Test
    fun `getPocById should return POC when exists`() {
        // Given
        val poc = createTestPoc()
        whenever(pocRepository.findById(testPocId)).thenReturn(Optional.of(poc))

        // When
        val result = pocService.getPocById(testPocId)

        // Then
        assertNotNull(result)
        assertEquals(testPocId, result.id)
        verify(pocRepository).findById(testPocId)
    }

    @Test
    fun `getPocById should throw ResourceNotFoundException when POC not found`() {
        // Given
        whenever(pocRepository.findById(testPocId)).thenReturn(Optional.empty())

        // When & Then
        val exception = assertThrows(ResourceNotFoundException::class.java) {
            pocService.getPocById(testPocId)
        }
        assertTrue(exception.message!!.contains("POC with ID $testPocId not found"))
    }

    // ========== UPDATE POC TESTS ==========

    @Test
    fun `updatePoc should update non-terminal POC successfully`() {
        // Given
        val existingPoc = createTestPoc(phase = Phase.PLANNING)
        val updateRequest = createUpdateRequest()
        val updatedPoc = existingPoc.copy(
            customerName = updateRequest.customerName,
            title = updateRequest.title
        )

        doReturn(Optional.of(existingPoc)).whenever(pocRepository).findById(testPocId)
        doReturn(true).whenever(userRepository).existsById(testOwnerId)
        doReturn(updatedPoc).whenever(pocRepository).save(any())

        // When
        val result = pocService.updatePoc(testPocId, updateRequest)

        // Then
        assertNotNull(result)
        assertEquals("Updated Corp", result.customerName)
        verify(pocRepository).save(any())
    }

    @Test
    fun `updatePoc should throw ValidationException for terminal phase POC`() {
        // Given
        val terminalPoc = createTestPoc(phase = Phase.CLOSED_WON)
        val updateRequest = createUpdateRequest()

        whenever(pocRepository.findById(testPocId)).thenReturn(Optional.of(terminalPoc))

        // When & Then
        val exception = assertThrows(ValidationException::class.java) {
            pocService.updatePoc(testPocId, updateRequest)
        }
        assertTrue(exception.message!!.contains("Cannot update POC in terminal phase"))
        verify(pocRepository, never()).save(any())
    }

    @Test
    fun `updatePoc should throw ValidationException when owner does not exist`() {
        // Given
        val existingPoc = createTestPoc()
        val updateRequest = createUpdateRequest()

        whenever(pocRepository.findById(testPocId)).thenReturn(Optional.of(existingPoc))
        whenever(userRepository.existsById(testOwnerId)).thenReturn(false)

        // When & Then
        val exception = assertThrows(ValidationException::class.java) {
            pocService.updatePoc(testPocId, updateRequest)
        }
        assertTrue(exception.message!!.contains("Owner with ID"))
        verify(pocRepository, never()).save(any())
    }

    @Test
    fun `updatePoc should throw ValidationException when endDate is before kickoffDate`() {
        // Given
        val existingPoc = createTestPoc()
        val updateRequest = createUpdateRequest().copy(
            kickoffDate = LocalDate.now(),
            endDate = LocalDate.now().minusDays(1)
        )

        whenever(pocRepository.findById(testPocId)).thenReturn(Optional.of(existingPoc))
        whenever(userRepository.existsById(testOwnerId)).thenReturn(true)

        // When & Then
        val exception = assertThrows(ValidationException::class.java) {
            pocService.updatePoc(testPocId, updateRequest)
        }
        assertTrue(exception.message!!.contains("End date must be on or after kickoff date"))
    }

    // ========== DELETE POC TESTS ==========

    @Test
    fun `deletePoc should delete existing POC`() {
        // Given
        whenever(pocRepository.existsById(testPocId)).thenReturn(true)

        // When
        pocService.deletePoc(testPocId)

        // Then
        verify(pocRepository).existsById(testPocId)
        verify(pocRepository).deleteById(testPocId)
    }

    @Test
    fun `deletePoc should throw ResourceNotFoundException when POC not found`() {
        // Given
        whenever(pocRepository.existsById(testPocId)).thenReturn(false)

        // When & Then
        val exception = assertThrows(ResourceNotFoundException::class.java) {
            pocService.deletePoc(testPocId)
        }
        assertTrue(exception.message!!.contains("POC with ID $testPocId not found"))
        verify(pocRepository, never()).deleteById(any())
    }

    // ========== STATUS CALCULATION TESTS ==========

    @Test
    fun `calculateStatus should return CLOSED for terminal phase`() {
        // Given
        val poc = createTestPoc(phase = Phase.CLOSED_WON, status = Status.ACTIVE)
        doReturn(poc.copy(status = Status.CLOSED)).whenever(pocRepository).save(any())

        // When
        val result = pocService.calculateStatus(poc)

        // Then
        assertEquals(Status.CLOSED, result)
        verify(pocRepository).save(any())
    }

    @Test
    fun `calculateStatus should return AT_RISK when past end date`() {
        // Given
        val pastEndDate = LocalDate.now().minusDays(1)
        val pastKickoffDate = LocalDate.now().minusDays(30)
        val poc = createTestPoc(
            phase = Phase.PLANNING,
            status = Status.ACTIVE,
            kickoffDate = pastKickoffDate,
            endDate = pastEndDate
        )
        doReturn(poc.copy(status = Status.AT_RISK)).whenever(pocRepository).save(any())

        // When
        val result = pocService.calculateStatus(poc)

        // Then
        assertEquals(Status.AT_RISK, result)
        verify(pocRepository).save(any())
    }

    @Test
    fun `calculateStatus should return ACTIVE for non-terminal phase within end date`() {
        // Given
        val poc = createTestPoc(
            phase = Phase.DISCOVERY,
            status = Status.ACTIVE,
            endDate = LocalDate.now().plusDays(10)
        )

        // When
        val result = pocService.calculateStatus(poc)

        // Then
        assertEquals(Status.ACTIVE, result)
        // Should not save if status hasn't changed
        verify(pocRepository, never()).save(any())
    }

    @Test
    fun `calculateStatus should update POC status in database when changed`() {
        // Given
        val poc = createTestPoc(phase = Phase.CLOSED_LOST, status = Status.ACTIVE)
        doReturn(poc.copy(status = Status.CLOSED)).whenever(pocRepository).save(any())

        // When
        pocService.calculateStatus(poc)

        // Then
        verify(pocRepository).save(argThat { status == Status.CLOSED })
    }

    // ========== FILTER POCS TESTS ==========

    @Test
    fun `filterPocs should filter by phase`() {
        // Given
        val pocs = listOf(
            createTestPoc(id = 1, phase = Phase.DISCOVERY),
            createTestPoc(id = 2, phase = Phase.PLANNING),
            createTestPoc(id = 3, phase = Phase.DISCOVERY)
        )
        whenever(pocRepository.findAll()).thenReturn(pocs)

        // When
        val result = pocService.filterPocs(phase = Phase.DISCOVERY)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.currentPhase == Phase.DISCOVERY })
    }

    @Test
    fun `filterPocs should filter by ownerId`() {
        // Given
        val pocs = listOf(
            createTestPoc(id = 1).copy(ownerId = 1L),
            createTestPoc(id = 2).copy(ownerId = 2L),
            createTestPoc(id = 3).copy(ownerId = 1L)
        )
        whenever(pocRepository.findAll()).thenReturn(pocs)

        // When
        val result = pocService.filterPocs(ownerId = 1L)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.ownerId == 1L })
    }

    @Test
    fun `filterPocs should filter by status`() {
        // Given
        val pocs = listOf(
            createTestPoc(id = 1, status = Status.ACTIVE),
            createTestPoc(id = 2, status = Status.AT_RISK),
            createTestPoc(id = 3, status = Status.ACTIVE)
        )
        whenever(pocRepository.findAll()).thenReturn(pocs)

        // When
        val result = pocService.filterPocs(status = Status.ACTIVE)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.status == Status.ACTIVE })
    }

    @Test
    fun `filterPocs should search by customer name`() {
        // Given
        val pocs = listOf(
            createTestPoc(id = 1).copy(customerName = "Acme Corp"),
            createTestPoc(id = 2).copy(customerName = "TechStart Inc"),
            createTestPoc(id = 3).copy(customerName = "Global Acme")
        )
        whenever(pocRepository.findAll()).thenReturn(pocs)

        // When
        val result = pocService.filterPocs(search = "acme")

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.customerName.contains("Acme", ignoreCase = true) })
    }

    @Test
    fun `filterPocs should search by title`() {
        // Given
        val pocs = listOf(
            createTestPoc(id = 1).copy(title = "Cloud Migration POC"),
            createTestPoc(id = 2).copy(title = "Security Assessment"),
            createTestPoc(id = 3).copy(title = "Cloud Storage POC")
        )
        whenever(pocRepository.findAll()).thenReturn(pocs)

        // When
        val result = pocService.filterPocs(search = "cloud")

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.title.contains("Cloud", ignoreCase = true) })
    }

    @Test
    fun `filterPocs should apply multiple filters`() {
        // Given
        val pocs = listOf(
            createTestPoc(id = 1, phase = Phase.DISCOVERY, status = Status.ACTIVE)
                .copy(ownerId = 1L, customerName = "Acme Corp"),
            createTestPoc(id = 2, phase = Phase.PLANNING, status = Status.ACTIVE)
                .copy(ownerId = 1L, customerName = "Acme Corp"),
            createTestPoc(id = 3, phase = Phase.DISCOVERY, status = Status.AT_RISK)
                .copy(ownerId = 1L, customerName = "Acme Corp")
        )
        whenever(pocRepository.findAll()).thenReturn(pocs)

        // When
        val result = pocService.filterPocs(
            phase = Phase.DISCOVERY,
            ownerId = 1L,
            status = Status.ACTIVE,
            search = "acme"
        )

        // Then
        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
    }

    // ========== ADVANCE PHASE TESTS ==========

    @Test
    fun `advancePhase should advance phase when all requirements complete`() {
        // Given
        val poc = createTestPoc(phase = Phase.DISCOVERY)
        val completeRequirements = listOf(
            Requirement(
                id = 1L,
                pocId = testPocId,
                phase = Phase.DISCOVERY,
                description = "Requirement 1",
                completed = true,
                displayOrder = 1
            )
        )
        val advancedPoc = poc.copy(currentPhase = Phase.PLANNING)

        doReturn(Optional.of(poc), Optional.of(advancedPoc)).whenever(pocRepository).findById(testPocId)
        doReturn(completeRequirements).whenever(requirementRepository).findByPocIdAndPhaseOrderByDisplayOrder(testPocId, Phase.DISCOVERY)
        doReturn(advancedPoc).whenever(pocRepository).save(any())

        // When
        val result = pocService.advancePhase(testPocId)

        // Then
        assertNotNull(result)
        verify(pocRepository, atLeast(1)).save(any())
    }

    @Test
    fun `advancePhase should throw PhaseTransitionException when requirements incomplete`() {
        // Given
        val poc = createTestPoc(phase = Phase.DISCOVERY)
        val incompleteRequirements = listOf(
            Requirement(
                id = 1L,
                pocId = testPocId,
                phase = Phase.DISCOVERY,
                description = "Incomplete Requirement",
                completed = false,
                displayOrder = 1
            )
        )

        whenever(pocRepository.findById(testPocId)).thenReturn(Optional.of(poc))
        whenever(requirementRepository.findByPocIdAndPhaseOrderByDisplayOrder(testPocId, Phase.DISCOVERY))
            .thenReturn(incompleteRequirements)

        // When & Then
        val exception = assertThrows(PhaseTransitionException::class.java) {
            pocService.advancePhase(testPocId)
        }
        assertTrue(exception.message!!.contains("Cannot advance phase"))
        assertTrue(exception.message!!.contains("Incomplete requirements"))
        verify(pocRepository, never()).save(any())
    }

    @Test
    fun `advancePhase should throw PhaseTransitionException for terminal phase`() {
        // Given
        val terminalPoc = createTestPoc(phase = Phase.CLOSED_WON)

        whenever(pocRepository.findById(testPocId)).thenReturn(Optional.of(terminalPoc))

        // When & Then
        val exception = assertThrows(PhaseTransitionException::class.java) {
            pocService.advancePhase(testPocId)
        }
        assertTrue(exception.message!!.contains("Cannot advance from terminal phase"))
        verify(requirementRepository, never()).findByPocIdAndPhaseOrderByDisplayOrder(any(), any())
    }

    @Test
    fun `advancePhase should throw ResourceNotFoundException when POC not found`() {
        // Given
        whenever(pocRepository.findById(testPocId)).thenReturn(Optional.empty())

        // When & Then
        assertThrows(ResourceNotFoundException::class.java) {
            pocService.advancePhase(testPocId)
        }
    }

    // ========== CLOSE POC TESTS ==========

    @Test
    fun `closePoc should close POC as WON`() {
        // Given
        val poc = createTestPoc(phase = Phase.EXECUTION)
        val closedPoc = poc.copy(currentPhase = Phase.CLOSED_WON, status = Status.CLOSED)
        val testUser = createTestUser()

        doReturn(Optional.of(poc)).whenever(pocRepository).findById(testPocId)
        doReturn(Optional.of(testUser)).whenever(userRepository).findById(testOwnerId)
        doReturn(closedPoc).whenever(pocRepository).save(any())

        // When
        val result = pocService.closePoc(testPocId, "WON", "Great success")

        // Then
        assertNotNull(result)
        assertEquals(Phase.CLOSED_WON, result.currentPhase)
        assertEquals(Status.CLOSED, result.status)
        verify(pocRepository).save(argThat {
            currentPhase == Phase.CLOSED_WON && status == Status.CLOSED
        })
    }

    @Test
    fun `closePoc should close POC as LOST`() {
        // Given
        val poc = createTestPoc(phase = Phase.PLANNING)
        val closedPoc = poc.copy(currentPhase = Phase.CLOSED_LOST, status = Status.CLOSED)
        val testUser = createTestUser()

        doReturn(Optional.of(poc)).whenever(pocRepository).findById(testPocId)
        doReturn(Optional.of(testUser)).whenever(userRepository).findById(testOwnerId)
        doReturn(closedPoc).whenever(pocRepository).save(any())

        // When
        val result = pocService.closePoc(testPocId, "LOST", "Budget constraints")

        // Then
        assertNotNull(result)
        assertEquals(Phase.CLOSED_LOST, result.currentPhase)
        assertEquals(Status.CLOSED, result.status)
        verify(pocRepository).save(argThat {
            currentPhase == Phase.CLOSED_LOST && status == Status.CLOSED
        })
    }

    @Test
    fun `closePoc should accept lowercase outcome`() {
        // Given
        val poc = createTestPoc()
        val closedPoc = poc.copy(currentPhase = Phase.CLOSED_WON, status = Status.CLOSED)
        val testUser = createTestUser()

        doReturn(Optional.of(poc)).whenever(pocRepository).findById(testPocId)
        doReturn(Optional.of(testUser)).whenever(userRepository).findById(testOwnerId)
        doReturn(closedPoc).whenever(pocRepository).save(any())

        // When
        val result = pocService.closePoc(testPocId, "won", null)

        // Then
        assertEquals(Phase.CLOSED_WON, result.currentPhase)
    }

    @Test
    fun `closePoc should throw ValidationException for invalid outcome`() {
        // Given
        val poc = createTestPoc()

        whenever(pocRepository.findById(testPocId)).thenReturn(Optional.of(poc))

        // When & Then
        val exception = assertThrows(ValidationException::class.java) {
            pocService.closePoc(testPocId, "INVALID", null)
        }
        assertTrue(exception.message!!.contains("Outcome must be either WON or LOST"))
        verify(pocRepository, never()).save(any())
    }

    @Test
    fun `closePoc should throw ResourceNotFoundException when POC not found`() {
        // Given
        whenever(pocRepository.findById(testPocId)).thenReturn(Optional.empty())

        // When & Then
        assertThrows(ResourceNotFoundException::class.java) {
            pocService.closePoc(testPocId, "WON", null)
        }
    }
}
