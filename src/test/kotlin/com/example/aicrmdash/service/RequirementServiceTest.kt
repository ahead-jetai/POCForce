package com.example.aicrmdash.service

import com.example.aicrmdash.config.DefaultRequirements
import com.example.aicrmdash.domain.Phase
import com.example.aicrmdash.domain.Poc
import com.example.aicrmdash.domain.Requirement
import com.example.aicrmdash.domain.Status
import com.example.aicrmdash.exception.ResourceNotFoundException
import com.example.aicrmdash.exception.ValidationException
import com.example.aicrmdash.repository.PocRepository
import com.example.aicrmdash.repository.RequirementRepository
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
 * Unit tests for RequirementService.
 *
 * Tests requirement retrieval, updates, completion tracking, and default requirement generation.
 */
@ExtendWith(MockitoExtension::class)
class RequirementServiceTest {

    @Mock
    private lateinit var requirementRepository: RequirementRepository

    @Mock
    private lateinit var pocRepository: PocRepository

    @InjectMocks
    private lateinit var requirementService: RequirementService

    // Test data
    private val testPocId = 100L
    private val testRequirementId = 200L

    private fun createTestPoc(
        id: Long = testPocId,
        phase: Phase = Phase.DISCOVERY
    ): Poc {
        return Poc(
            id = id,
            customerName = "Test Corp",
            title = "Test POC",
            description = "Test description",
            dealValue = BigDecimal("50000.00"),
            projectedCloseDate = LocalDate.now().plusMonths(3),
            kickoffDate = LocalDate.now(),
            endDate = LocalDate.now().plusMonths(2),
            currentPhase = phase,
            ownerId = 1L,
            status = Status.ACTIVE,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun createTestRequirement(
        id: Long = testRequirementId,
        pocId: Long = testPocId,
        phase: Phase = Phase.DISCOVERY,
        description: String = "Test requirement",
        completed: Boolean = false,
        completedAt: LocalDateTime? = null,
        notes: String? = null,
        displayOrder: Int = 1
    ): Requirement {
        return Requirement(
            id = id,
            pocId = pocId,
            phase = phase,
            description = description,
            completed = completed,
            completedAt = completedAt,
            notes = notes,
            displayOrder = displayOrder
        )
    }

    // ========== GET REQUIREMENTS TESTS ==========

    @Test
    fun `getRequirementsForPoc should return requirements for current phase`() {
        // Given
        val poc = createTestPoc(phase = Phase.PLANNING)
        val requirements = listOf(
            createTestRequirement(id = 1L, phase = Phase.PLANNING, displayOrder = 1),
            createTestRequirement(id = 2L, phase = Phase.PLANNING, displayOrder = 2)
        )

        doReturn(Optional.of(poc)).whenever(pocRepository).findById(testPocId)
        doReturn(requirements).whenever(requirementRepository)
            .findByPocIdAndPhaseOrderByDisplayOrder(testPocId, Phase.PLANNING)

        // When
        val result = requirementService.getRequirementsForPoc(testPocId)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.phase == Phase.PLANNING })
        verify(pocRepository).findById(testPocId)
        verify(requirementRepository).findByPocIdAndPhaseOrderByDisplayOrder(testPocId, Phase.PLANNING)
    }

    @Test
    fun `getRequirementsForPoc should throw ResourceNotFoundException when POC not found`() {
        // Given
        doReturn(Optional.empty<Poc>()).whenever(pocRepository).findById(testPocId)

        // When & Then
        val exception = assertThrows(ResourceNotFoundException::class.java) {
            requirementService.getRequirementsForPoc(testPocId)
        }
        assertTrue(exception.message!!.contains("POC not found"))
    }

    @Test
    fun `getRequirementsForPoc should return empty list when no requirements exist`() {
        // Given
        val poc = createTestPoc(phase = Phase.DISCOVERY)
        doReturn(Optional.of(poc)).whenever(pocRepository).findById(testPocId)
        doReturn(emptyList<Requirement>()).whenever(requirementRepository)
            .findByPocIdAndPhaseOrderByDisplayOrder(testPocId, Phase.DISCOVERY)

        // When
        val result = requirementService.getRequirementsForPoc(testPocId)

        // Then
        assertTrue(result.isEmpty())
    }

    // ========== UPDATE REQUIREMENT TESTS ==========

    @Test
    fun `updateRequirement should mark requirement as complete and set completedAt`() {
        // Given
        val poc = createTestPoc()
        val requirement = createTestRequirement(completed = false, completedAt = null)
        val updatedRequirement = requirement.copy(completed = true, completedAt = LocalDateTime.now())

        doReturn(Optional.of(poc)).whenever(pocRepository).findById(testPocId)
        doReturn(Optional.of(requirement)).whenever(requirementRepository).findById(testRequirementId)
        doReturn(updatedRequirement).whenever(requirementRepository).save(any())

        // When
        val result = requirementService.updateRequirement(testPocId, testRequirementId, completed = true, notes = null)

        // Then
        assertTrue(result.completed)
        assertNotNull(result.completedAt)
        verify(requirementRepository).save(argThat { 
            completed && completedAt != null 
        })
    }

    @Test
    fun `updateRequirement should mark requirement as incomplete and clear completedAt`() {
        // Given
        val poc = createTestPoc()
        val requirement = createTestRequirement(completed = true, completedAt = LocalDateTime.now())
        val updatedRequirement = requirement.copy(completed = false, completedAt = null)

        doReturn(Optional.of(poc)).whenever(pocRepository).findById(testPocId)
        doReturn(Optional.of(requirement)).whenever(requirementRepository).findById(testRequirementId)
        doReturn(updatedRequirement).whenever(requirementRepository).save(any())

        // When
        val result = requirementService.updateRequirement(testPocId, testRequirementId, completed = false, notes = null)

        // Then
        assertFalse(result.completed)
        assertNull(result.completedAt)
        verify(requirementRepository).save(argThat { 
            !completed && completedAt == null 
        })
    }

    @Test
    fun `updateRequirement should update notes`() {
        // Given
        val poc = createTestPoc()
        val requirement = createTestRequirement(notes = null)
        val newNotes = "Updated notes"
        val updatedRequirement = requirement.copy(notes = newNotes)

        doReturn(Optional.of(poc)).whenever(pocRepository).findById(testPocId)
        doReturn(Optional.of(requirement)).whenever(requirementRepository).findById(testRequirementId)
        doReturn(updatedRequirement).whenever(requirementRepository).save(any())

        // When
        val result = requirementService.updateRequirement(testPocId, testRequirementId, completed = null, notes = newNotes)

        // Then
        assertEquals(newNotes, result.notes)
        verify(requirementRepository).save(argThat { notes == newNotes })
    }

    @Test
    fun `updateRequirement should preserve completedAt when not changing completion status`() {
        // Given
        val poc = createTestPoc()
        val existingCompletedAt = LocalDateTime.now().minusDays(1)
        val requirement = createTestRequirement(completed = true, completedAt = existingCompletedAt)
        val updatedRequirement = requirement.copy(notes = "New notes")

        doReturn(Optional.of(poc)).whenever(pocRepository).findById(testPocId)
        doReturn(Optional.of(requirement)).whenever(requirementRepository).findById(testRequirementId)
        doReturn(updatedRequirement).whenever(requirementRepository).save(any())

        // When
        val result = requirementService.updateRequirement(testPocId, testRequirementId, completed = null, notes = "New notes")

        // Then
        assertEquals(existingCompletedAt, result.completedAt)
    }

    @Test
    fun `updateRequirement should throw ValidationException when notes exceed 1000 characters`() {
        // Given
        val poc = createTestPoc()
        val requirement = createTestRequirement()
        val longNotes = "a".repeat(1001)

        doReturn(Optional.of(poc)).whenever(pocRepository).findById(testPocId)
        doReturn(Optional.of(requirement)).whenever(requirementRepository).findById(testRequirementId)

        // When & Then
        val exception = assertThrows(ValidationException::class.java) {
            requirementService.updateRequirement(testPocId, testRequirementId, completed = null, notes = longNotes)
        }
        assertTrue(exception.message!!.contains("Notes must not exceed 1000 characters"))
        verify(requirementRepository, never()).save(any())
    }

    @Test
    fun `updateRequirement should throw ResourceNotFoundException when POC not found`() {
        // Given
        doReturn(Optional.empty<Poc>()).whenever(pocRepository).findById(testPocId)

        // When & Then
        val exception = assertThrows(ResourceNotFoundException::class.java) {
            requirementService.updateRequirement(testPocId, testRequirementId, completed = true, notes = null)
        }
        assertTrue(exception.message!!.contains("POC not found"))
    }

    @Test
    fun `updateRequirement should throw ResourceNotFoundException when requirement not found`() {
        // Given
        val poc = createTestPoc()
        doReturn(Optional.of(poc)).whenever(pocRepository).findById(testPocId)
        doReturn(Optional.empty<Requirement>()).whenever(requirementRepository).findById(testRequirementId)

        // When & Then
        val exception = assertThrows(ResourceNotFoundException::class.java) {
            requirementService.updateRequirement(testPocId, testRequirementId, completed = true, notes = null)
        }
        assertTrue(exception.message!!.contains("Requirement not found"))
    }

    @Test
    fun `updateRequirement should throw ValidationException when requirement does not belong to POC`() {
        // Given
        val poc = createTestPoc(id = testPocId)
        val requirement = createTestRequirement(pocId = 999L) // Different POC ID

        doReturn(Optional.of(poc)).whenever(pocRepository).findById(testPocId)
        doReturn(Optional.of(requirement)).whenever(requirementRepository).findById(testRequirementId)

        // When & Then
        val exception = assertThrows(ValidationException::class.java) {
            requirementService.updateRequirement(testPocId, testRequirementId, completed = true, notes = null)
        }
        assertTrue(exception.message!!.contains("does not belong to POC"))
        verify(requirementRepository, never()).save(any())
    }

    // ========== CALCULATE COMPLETION PERCENTAGE TESTS ==========

    @Test
    fun `calculateCompletionPercentage should return 0 when no requirements exist`() {
        // Given
        val poc = createTestPoc()
        doReturn(Optional.of(poc)).whenever(pocRepository).findById(testPocId)
        doReturn(emptyList<Requirement>()).whenever(requirementRepository)
            .findByPocIdAndPhaseOrderByDisplayOrder(testPocId, poc.currentPhase)

        // When
        val result = requirementService.calculateCompletionPercentage(testPocId)

        // Then
        assertEquals(0.0, result)
    }

    @Test
    fun `calculateCompletionPercentage should return 0 when no requirements completed`() {
        // Given
        val poc = createTestPoc()
        val requirements = listOf(
            createTestRequirement(id = 1L, completed = false),
            createTestRequirement(id = 2L, completed = false),
            createTestRequirement(id = 3L, completed = false)
        )

        doReturn(Optional.of(poc)).whenever(pocRepository).findById(testPocId)
        doReturn(requirements).whenever(requirementRepository)
            .findByPocIdAndPhaseOrderByDisplayOrder(testPocId, poc.currentPhase)

        // When
        val result = requirementService.calculateCompletionPercentage(testPocId)

        // Then
        assertEquals(0.0, result)
    }

    @Test
    fun `calculateCompletionPercentage should return 100 when all requirements completed`() {
        // Given
        val poc = createTestPoc()
        val requirements = listOf(
            createTestRequirement(id = 1L, completed = true),
            createTestRequirement(id = 2L, completed = true),
            createTestRequirement(id = 3L, completed = true)
        )

        doReturn(Optional.of(poc)).whenever(pocRepository).findById(testPocId)
        doReturn(requirements).whenever(requirementRepository)
            .findByPocIdAndPhaseOrderByDisplayOrder(testPocId, poc.currentPhase)

        // When
        val result = requirementService.calculateCompletionPercentage(testPocId)

        // Then
        assertEquals(100.0, result)
    }

    @Test
    fun `calculateCompletionPercentage should return correct percentage for partial completion`() {
        // Given
        val poc = createTestPoc()
        val requirements = listOf(
            createTestRequirement(id = 1L, completed = true),
            createTestRequirement(id = 2L, completed = false),
            createTestRequirement(id = 3L, completed = true),
            createTestRequirement(id = 4L, completed = false)
        )

        doReturn(Optional.of(poc)).whenever(pocRepository).findById(testPocId)
        doReturn(requirements).whenever(requirementRepository)
            .findByPocIdAndPhaseOrderByDisplayOrder(testPocId, poc.currentPhase)

        // When
        val result = requirementService.calculateCompletionPercentage(testPocId)

        // Then
        assertEquals(50.0, result)
    }

    @Test
    fun `calculateCompletionPercentage should throw ResourceNotFoundException when POC not found`() {
        // Given
        doReturn(Optional.empty<Poc>()).whenever(pocRepository).findById(testPocId)

        // When & Then
        assertThrows(ResourceNotFoundException::class.java) {
            requirementService.calculateCompletionPercentage(testPocId)
        }
    }

    // ========== GENERATE DEFAULT REQUIREMENTS TESTS ==========

    @Test
    fun `generateDefaultRequirements should create requirements for DISCOVERY phase`() {
        // Given
        val poc = createTestPoc(phase = Phase.DISCOVERY)
        val defaultDescriptions = DefaultRequirements.getRequirementsForPhase(Phase.DISCOVERY)
        val generatedRequirements = defaultDescriptions.mapIndexed { index, desc ->
            createTestRequirement(
                id = (index + 1).toLong(),
                phase = Phase.DISCOVERY,
                description = desc,
                displayOrder = index + 1
            )
        }

        doReturn(Optional.of(poc)).whenever(pocRepository).findById(testPocId)
        doReturn(generatedRequirements).whenever(requirementRepository).saveAll(any<List<Requirement>>())

        // When
        val result = requirementService.generateDefaultRequirements(testPocId, Phase.DISCOVERY)

        // Then
        assertEquals(defaultDescriptions.size, result.size)
        assertTrue(result.all { it.phase == Phase.DISCOVERY })
        assertTrue(result.all { !it.completed })
        verify(requirementRepository).saveAll(argThat<List<Requirement>> { 
            size == defaultDescriptions.size && all { it.phase == Phase.DISCOVERY }
        })
    }

    @Test
    fun `generateDefaultRequirements should create requirements for each phase`() {
        // Test each phase
        Phase.entries.filter { !it.isTerminal() }.forEach { phase ->
            // Given
            val poc = createTestPoc(phase = phase)
            val defaultDescriptions = DefaultRequirements.getRequirementsForPhase(phase)
            val generatedRequirements = defaultDescriptions.mapIndexed { index, desc ->
                createTestRequirement(
                    id = (index + 1).toLong(),
                    phase = phase,
                    description = desc,
                    displayOrder = index + 1
                )
            }

            doReturn(Optional.of(poc)).whenever(pocRepository).findById(testPocId)
            doReturn(generatedRequirements).whenever(requirementRepository).saveAll(any<List<Requirement>>())

            // When
            val result = requirementService.generateDefaultRequirements(testPocId, phase)

            // Then
            assertEquals(defaultDescriptions.size, result.size)
            assertTrue(result.all { it.phase == phase })
        }
    }

    @Test
    fun `generateDefaultRequirements should assign sequential display orders`() {
        // Given
        val poc = createTestPoc()
        val defaultDescriptions = DefaultRequirements.getRequirementsForPhase(Phase.PLANNING)
        val generatedRequirements = defaultDescriptions.mapIndexed { index, desc ->
            createTestRequirement(
                id = (index + 1).toLong(),
                phase = Phase.PLANNING,
                description = desc,
                displayOrder = index + 1
            )
        }

        doReturn(Optional.of(poc)).whenever(pocRepository).findById(testPocId)
        doReturn(generatedRequirements).whenever(requirementRepository).saveAll(any<List<Requirement>>())

        // When
        val result = requirementService.generateDefaultRequirements(testPocId, Phase.PLANNING)

        // Then
        result.forEachIndexed { index, requirement ->
            assertEquals(index + 1, requirement.displayOrder)
        }
    }

    @Test
    fun `generateDefaultRequirements should throw ResourceNotFoundException when POC not found`() {
        // Given
        doReturn(Optional.empty<Poc>()).whenever(pocRepository).findById(testPocId)

        // When & Then
        val exception = assertThrows(ResourceNotFoundException::class.java) {
            requirementService.generateDefaultRequirements(testPocId, Phase.DISCOVERY)
        }
        assertTrue(exception.message!!.contains("POC not found"))
        verify(requirementRepository, never()).saveAll(any<List<Requirement>>())
    }
}
