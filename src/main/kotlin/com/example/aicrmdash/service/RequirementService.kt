package com.example.aicrmdash.service

import com.example.aicrmdash.config.DefaultRequirements
import com.example.aicrmdash.domain.Phase
import com.example.aicrmdash.domain.Requirement
import com.example.aicrmdash.exception.ResourceNotFoundException
import com.example.aicrmdash.exception.ValidationException
import com.example.aicrmdash.repository.PocRepository
import com.example.aicrmdash.repository.RequirementRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Service layer for managing POC requirements and phase checklists.
 *
 * Handles requirement retrieval, updates, completion tracking, and generation of default requirements.
 */
@Service
@Transactional
class RequirementService(
    private val requirementRepository: RequirementRepository,
    private val pocRepository: PocRepository
) {

    private val logger = LoggerFactory.getLogger(RequirementService::class.java)

    /**
     * Retrieves all requirements for a POC's current phase.
     *
     * @param pocId The POC ID
     * @return List of requirements for the current phase, ordered by display order
     * @throws ResourceNotFoundException if POC not found
     */
    fun getRequirementsForPoc(pocId: Long): List<Requirement> {
        val poc = pocRepository.findById(pocId)
            .orElseThrow { ResourceNotFoundException("POC not found with id: $pocId") }
        
        return requirementRepository.findByPocIdAndPhaseOrderByDisplayOrder(pocId, poc.currentPhase)
    }

    /**
     * Updates a requirement's completion status and/or notes.
     *
     * Implements FR-23: Set completedAt timestamp when marking complete
     * Implements FR-24: Clear completedAt when marking incomplete
     * Implements FR-25: Validate notes length
     *
     * @param pocId The POC ID (for validation)
     * @param requirementId The requirement ID to update
     * @param completed Optional new completion status
     * @param notes Optional notes to update
     * @return Updated requirement
     * @throws ResourceNotFoundException if POC or requirement not found
     * @throws ValidationException if requirement doesn't belong to the POC or notes validation fails
     */
    fun updateRequirement(
        pocId: Long,
        requirementId: Long,
        completed: Boolean?,
        notes: String?
    ): Requirement {
        logger.info("Updating requirement: id={}, pocId={}, completed={}", 
            requirementId, pocId, completed)
        
        // Validate POC exists
        pocRepository.findById(pocId)
            .orElseThrow { ResourceNotFoundException("POC not found with id: $pocId") }
        
        // Find requirement
        val requirement = requirementRepository.findById(requirementId)
            .orElseThrow { ResourceNotFoundException("Requirement not found with id: $requirementId") }
        
        // Validate requirement belongs to the POC
        if (requirement.pocId != pocId) {
            logger.warn("Requirement update failed: Requirement {} does not belong to POC {}", 
                requirementId, pocId)
            throw ValidationException("Requirement $requirementId does not belong to POC $pocId")
        }
        
        // Validate notes length if provided
        if (notes != null && notes.length > 1000) {
            logger.warn("Requirement update failed: Notes exceed 1000 characters for requirement {}", 
                requirementId)
            throw ValidationException("Notes must not exceed 1000 characters")
        }
        
        // Update completion status
        val updatedCompleted = completed ?: requirement.completed
        val updatedCompletedAt = when {
            completed == true && !requirement.completed -> LocalDateTime.now() // Mark as complete
            completed == false && requirement.completed -> null // Mark as incomplete
            else -> requirement.completedAt // No change
        }
        
        // Update notes
        val updatedNotes = notes ?: requirement.notes
        
        // Create updated requirement
        val updatedRequirement = requirement.copy(
            completed = updatedCompleted,
            completedAt = updatedCompletedAt,
            notes = updatedNotes
        )
        
        val savedRequirement = requirementRepository.save(updatedRequirement)
        logger.info("Requirement updated successfully: id={}, completed={}", 
            requirementId, savedRequirement.completed)
        return savedRequirement
    }

    /**
     * Calculates the completion percentage for a POC's current phase.
     *
     * @param pocId The POC ID
     * @return Completion percentage (0.0 to 100.0)
     * @throws ResourceNotFoundException if POC not found
     */
    fun calculateCompletionPercentage(pocId: Long): Double {
        val requirements = getRequirementsForPoc(pocId)
        
        if (requirements.isEmpty()) {
            return 0.0
        }
        
        val completedCount = requirements.count { it.completed }
        return (completedCount.toDouble() / requirements.size) * 100.0
    }

    /**
     * Generates default requirements for a POC phase.
     *
     * Creates requirement entities based on the default templates for the specified phase.
     * Requirements are assigned sequential display orders.
     *
     * @param pocId The POC ID
     * @param phase The phase to generate requirements for
     * @return List of created requirements
     * @throws ResourceNotFoundException if POC not found
     */
    fun generateDefaultRequirements(pocId: Long, phase: Phase): List<Requirement> {
        // Validate POC exists
        pocRepository.findById(pocId)
            .orElseThrow { ResourceNotFoundException("POC not found with id: $pocId") }
        
        // Get default requirement descriptions for the phase
        val defaultDescriptions = DefaultRequirements.getRequirementsForPhase(phase)
        
        // Create requirement entities
        val requirements = defaultDescriptions.mapIndexed { index, description ->
            Requirement(
                pocId = pocId,
                phase = phase,
                description = description,
                completed = false,
                completedAt = null,
                notes = null,
                displayOrder = index + 1
            )
        }
        
        // Save and return
        return requirementRepository.saveAll(requirements)
    }
}
