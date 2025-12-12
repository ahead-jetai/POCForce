package com.example.aicrmdash.service

import com.example.aicrmdash.config.DefaultRequirements
import com.example.aicrmdash.domain.Phase
import com.example.aicrmdash.domain.PhaseTransition
import com.example.aicrmdash.domain.Poc
import com.example.aicrmdash.domain.Requirement
import com.example.aicrmdash.domain.Status
import com.example.aicrmdash.dto.CreatePocRequest
import com.example.aicrmdash.dto.PhaseTransitionResponse
import com.example.aicrmdash.dto.UpdatePocRequest
import com.example.aicrmdash.dto.UserResponse
import com.example.aicrmdash.exception.ResourceNotFoundException
import com.example.aicrmdash.exception.ValidationException
import com.example.aicrmdash.repository.PhaseTransitionRepository
import com.example.aicrmdash.repository.PocRepository
import com.example.aicrmdash.repository.RequirementRepository
import com.example.aicrmdash.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Service class for POC management operations.
 *
 * Provides business logic for creating, updating, retrieving, and deleting POCs.
 */
@Service
@Transactional
class PocService(
    private val pocRepository: PocRepository,
    private val userRepository: UserRepository,
    private val requirementRepository: RequirementRepository,
    private val phaseTransitionRepository: PhaseTransitionRepository
) {

    private val logger = LoggerFactory.getLogger(PocService::class.java)

    /**
     * Records a phase transition in the audit trail.
     *
     * @param pocId The POC ID
     * @param fromPhase The phase transitioning from (null for initial phase)
     * @param toPhase The phase transitioning to
     * @param userId The ID of the user who initiated the transition
     */
    private fun recordPhaseTransition(pocId: Long, fromPhase: Phase?, toPhase: Phase, userId: Long) {
        val user = userRepository.findById(userId).orElseThrow {
            ResourceNotFoundException("User with ID $userId not found")
        }
        
        val transition = PhaseTransition(
            pocId = pocId,
            fromPhase = fromPhase,
            toPhase = toPhase,
            transitionedAt = LocalDateTime.now(),
            transitionedBy = user
        )
        
        phaseTransitionRepository.save(transition)
        logger.info("Phase transition recorded: pocId={}, from={}, to={}, by user={}", 
            pocId, fromPhase, toPhase, userId)
    }

    /**
     * Creates a new POC with initial phase set to DISCOVERY and status set to ACTIVE.
     * Generates default requirements for the DISCOVERY phase.
     *
     * @param request The POC creation request
     * @return The created POC entity
     * @throws ValidationException if ownerId references invalid user or business rules are violated
     */
    fun createPoc(request: CreatePocRequest): Poc {
        logger.info("Creating new POC: customer={}, title={}, ownerId={}", 
            request.customerName, request.title, request.ownerId)
        
        // Validate owner exists
        if (!userRepository.existsById(request.ownerId)) {
            logger.warn("POC creation failed: Owner with ID {} does not exist", request.ownerId)
            throw ValidationException("Owner with ID ${request.ownerId} does not exist")
        }

        // Validate endDate >= kickoffDate
        if (request.endDate < request.kickoffDate) {
            logger.warn("POC creation failed: End date {} is before kickoff date {}", 
                request.endDate, request.kickoffDate)
            throw ValidationException("End date must be on or after kickoff date")
        }

        // Generate default requirements for DISCOVERY phase
        val requirements = DefaultRequirements.getRequirementsForPhase(Phase.DISCOVERY)
            .mapIndexed { index, description ->
                Requirement(
                    pocId = 0, // Will be set by JPA through the relationship
                    phase = Phase.DISCOVERY,
                    description = description,
                    completed = false,
                    displayOrder = index + 1
                )
            }

        // Create POC entity with requirements
        val poc = Poc(
            customerName = request.customerName,
            title = request.title,
            description = request.description,
            dealValue = request.dealValue,
            projectedCloseDate = request.projectedCloseDate,
            kickoffDate = request.kickoffDate,
            endDate = request.endDate,
            currentPhase = Phase.DISCOVERY,
            ownerId = request.ownerId,
            status = Status.ACTIVE,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            requirements = requirements.toMutableList()
        )

        val savedPoc = pocRepository.save(poc)

        // Record initial phase transition
        recordPhaseTransition(savedPoc.id, null, Phase.DISCOVERY, request.ownerId)

        logger.info("POC created successfully: id={}, customer={}, dealValue={}",
            savedPoc.id, savedPoc.customerName, savedPoc.dealValue)

        // Fetch the POC again to load the owner relationship
        return getPocById(savedPoc.id)
    }

    /**
     * Retrieves all POCs.
     *
     * @return List of all POCs
     */
    @Transactional(readOnly = true)
    fun getAllPocs(): List<Poc> {
        return pocRepository.findAll()
    }

    /**
     * Retrieves a POC by its ID.
     *
     * @param id The POC ID
     * @return The POC entity
     * @throws ResourceNotFoundException if POC is not found
     */
    @Transactional(readOnly = true)
    fun getPocById(id: Long): Poc {
        return pocRepository.findByIdWithOwner(id)
            .orElseThrow { ResourceNotFoundException("POC with ID $id not found") }
    }

    /**
     * Updates an existing POC.
     * Prevents updates to POCs in terminal phases (CLOSED_WON, CLOSED_LOST).
     *
     * @param id The POC ID
     * @param request The update request
     * @return The updated POC entity
     * @throws ResourceNotFoundException if POC is not found
     * @throws ValidationException if POC is in terminal phase or business rules are violated
     */
    fun updatePoc(id: Long, request: UpdatePocRequest): Poc {
        logger.info("Updating POC: id={}", id)
        val existingPoc = getPocById(id)

        // Prevent updates to POCs in terminal phases
        if (existingPoc.currentPhase.isTerminal()) {
            logger.warn("POC update failed: POC {} is in terminal phase {}", id, existingPoc.currentPhase)
            throw ValidationException("Cannot update POC in terminal phase ${existingPoc.currentPhase}")
        }

        // Validate owner exists
        if (!userRepository.existsById(request.ownerId)) {
            logger.warn("POC update failed: Owner with ID {} does not exist", request.ownerId)
            throw ValidationException("Owner with ID ${request.ownerId} does not exist")
        }

        // Validate endDate >= kickoffDate
        if (request.endDate < request.kickoffDate) {
            logger.warn("POC update failed: End date {} is before kickoff date {}", 
                request.endDate, request.kickoffDate)
            throw ValidationException("End date must be on or after kickoff date")
        }

        // Create updated POC
        val updatedPoc = existingPoc.copy(
            customerName = request.customerName,
            title = request.title,
            description = request.description,
            dealValue = request.dealValue,
            projectedCloseDate = request.projectedCloseDate,
            kickoffDate = request.kickoffDate,
            endDate = request.endDate,
            ownerId = request.ownerId,
            updatedAt = LocalDateTime.now()
        )

        pocRepository.save(updatedPoc)
        logger.info("POC updated successfully: id={}", id)
        return getPocById(id)
    }

    /**
     * Deletes a POC by its ID.
     * Cascade delete will also remove all associated requirements.
     *
     * @param id The POC ID
     * @throws ResourceNotFoundException if POC is not found
     */
    fun deletePoc(id: Long) {
        logger.info("Deleting POC: id={}", id)
        if (!pocRepository.existsById(id)) {
            logger.warn("POC deletion failed: POC with ID {} not found", id)
            throw ResourceNotFoundException("POC with ID $id not found")
        }
        pocRepository.deleteById(id)
        logger.info("POC deleted successfully: id={}", id)
    }

    /**
     * Calculates the status of a POC based on its current phase and end date.
     * Updates the POC status in the database if it has changed.
     *
     * Logic:
     * - If phase is terminal (CLOSED_WON, CLOSED_LOST) → CLOSED
     * - If current date > endDate and phase is non-terminal → AT_RISK
     * - Otherwise → ACTIVE
     *
     * @param poc The POC to calculate status for
     * @return The calculated status
     */
    fun calculateStatus(poc: Poc): Status {
        val calculatedStatus = when {
            poc.currentPhase.isTerminal() -> Status.CLOSED
            LocalDateTime.now().toLocalDate() > poc.endDate -> Status.AT_RISK
            else -> Status.ACTIVE
        }

        // Update POC status if it has changed
        if (poc.status != calculatedStatus) {
            val updatedPoc = poc.copy(
                status = calculatedStatus,
                updatedAt = LocalDateTime.now()
            )
            pocRepository.save(updatedPoc)
        }

        return calculatedStatus
    }

    /**
     * Filters and searches POCs based on provided criteria.
     * Recalculates statuses for all POCs before filtering.
     *
     * @param phase Filter by current phase (optional)
     * @param ownerId Filter by owner ID (optional)
     * @param status Filter by status (optional)
     * @param search Search in customer name or title (case-insensitive, partial match) (optional)
     * @return List of POCs matching the criteria
     */
    @Transactional(readOnly = true)
    fun filterPocs(
        phase: Phase? = null,
        ownerId: Long? = null,
        status: Status? = null,
        search: String? = null
    ): List<Poc> {
        var pocs = getAllPocs()

        // Recalculate statuses for all POCs
        pocs.forEach { calculateStatus(it) }

        // Reload POCs to get updated statuses
        pocs = getAllPocs()

        // Apply filters
        if (phase != null) {
            pocs = pocs.filter { it.currentPhase == phase }
        }

        if (ownerId != null) {
            pocs = pocs.filter { it.ownerId == ownerId }
        }

        if (status != null) {
            pocs = pocs.filter { it.status == status }
        }

        if (!search.isNullOrBlank()) {
            val searchLower = search.lowercase()
            pocs = pocs.filter {
                it.customerName.lowercase().contains(searchLower) ||
                it.title.lowercase().contains(searchLower)
            }
        }

        return pocs
    }

    /**
     * Advances a POC to the next phase.
     *
     * Implements FR-12, FR-13, FR-14, FR-15, FR-16:
     * - Validates all current phase requirements are complete
     * - Prevents transition from terminal phases
     * - Advances to next phase and generates new requirements
     *
     * @param pocId The POC ID
     * @return The updated POC
     * @throws ResourceNotFoundException if POC not found
     * @throws PhaseTransitionException if requirements incomplete or transition invalid
     */
    fun advancePhase(pocId: Long): Poc {
        logger.info("Advancing phase for POC: id={}", pocId)
        val poc = getPocById(pocId)

        // FR-16: Prevent transition from terminal phases
        if (poc.currentPhase.isTerminal()) {
            logger.warn("Phase advance failed: POC {} is in terminal phase {}", pocId, poc.currentPhase)
            throw com.example.aicrmdash.exception.PhaseTransitionException(
                "Cannot advance from terminal phase ${poc.currentPhase}"
            )
        }

        // FR-12, FR-13: Check all current phase requirements are complete
        val currentRequirements = requirementRepository.findByPocIdAndPhaseOrderByDisplayOrder(
            pocId, poc.currentPhase
        )
        val incompleteRequirements = currentRequirements.filter { !it.completed }

        // FR-14: If incomplete, throw exception with list of incomplete requirements
        if (incompleteRequirements.isNotEmpty()) {
            val incompleteDescriptions = incompleteRequirements.joinToString(", ") { it.description }
            logger.warn("Phase advance failed for POC {}: {} incomplete requirements", 
                pocId, incompleteRequirements.size)
            throw com.example.aicrmdash.exception.PhaseTransitionException(
                "Cannot advance phase. Incomplete requirements: $incompleteDescriptions"
            )
        }

        // FR-15: Advance to next phase
        val nextPhase = poc.currentPhase.nextPhase()
            ?: throw com.example.aicrmdash.exception.PhaseTransitionException(
                "No next phase available for ${poc.currentPhase}"
            )

        // Generate default requirements for new phase
        val newRequirements = DefaultRequirements.getRequirementsForPhase(nextPhase)
            .mapIndexed { index, description ->
                Requirement(
                    pocId = 0, // Will be set by JPA through the relationship
                    phase = nextPhase,
                    description = description,
                    completed = false,
                    displayOrder = index + 1
                )
            }

        // Update POC to next phase and add new requirements
        val updatedPoc = poc.copy(
            currentPhase = nextPhase,
            updatedAt = LocalDateTime.now(),
            requirements = (poc.requirements + newRequirements).toMutableList()
        )
        val savedPoc = pocRepository.save(updatedPoc)

        // Record phase transition
        recordPhaseTransition(pocId, poc.currentPhase, nextPhase, poc.ownerId)

        // Recalculate status
        calculateStatus(savedPoc)

        logger.info("POC phase advanced successfully: id={}, from {} to {}", 
            pocId, poc.currentPhase, nextPhase)
        return getPocById(pocId)
    }

    /**
     * Closes a POC with a final outcome (WON or LOST).
     *
     * Implements FR-17, FR-18:
     * - Sets phase to CLOSED_WON or CLOSED_LOST based on outcome
     * - Sets status to CLOSED
     *
     * @param pocId The POC ID
     * @param outcome The closure outcome ("WON" or "LOST")
     * @param notes Optional closure notes
     * @return The updated POC
     * @throws ResourceNotFoundException if POC not found
     * @throws ValidationException if outcome is invalid
     */
    fun closePoc(pocId: Long, outcome: String, notes: String?): Poc {
        logger.info("Closing POC: id={}, outcome={}", pocId, outcome)
        val poc = getPocById(pocId)

        // Validate outcome
        val closurePhase = when (outcome.uppercase()) {
            "WON" -> Phase.CLOSED_WON
            "LOST" -> Phase.CLOSED_LOST
            else -> {
                logger.warn("POC closure failed: Invalid outcome '{}' for POC {}", outcome, pocId)
                throw ValidationException("Outcome must be either WON or LOST")
            }
        }

        // Update POC to closed phase and status
        val updatedPoc = poc.copy(
            currentPhase = closurePhase,
            status = Status.CLOSED,
            updatedAt = LocalDateTime.now()
        )

        pocRepository.save(updatedPoc)

        // Record phase transition
        recordPhaseTransition(pocId, poc.currentPhase, closurePhase, poc.ownerId)

        logger.info("POC closed successfully: id={}, outcome={}, phase={}",
            pocId, outcome, closurePhase)
        return getPocById(pocId)
    }

    /**
     * Retrieves the phase transition history for a POC.
     *
     * @param pocId The POC ID
     * @return List of phase transitions ordered by transition timestamp
     * @throws ResourceNotFoundException if POC not found
     */
    @Transactional(readOnly = true)
    fun getPhaseTransitionHistory(pocId: Long): List<PhaseTransitionResponse> {
        logger.info("Retrieving phase transition history for POC: id={}", pocId)
        
        // Verify POC exists
        getPocById(pocId)
        
        // Retrieve phase transitions
        val transitions = phaseTransitionRepository.findByPocIdOrderByTransitionedAtAsc(pocId)
        
        // Map to DTOs
        return transitions.map { transition ->
            PhaseTransitionResponse(
                id = transition.id,
                pocId = transition.pocId,
                fromPhase = transition.fromPhase,
                toPhase = transition.toPhase,
                transitionedAt = transition.transitionedAt,
                transitionedBy = UserResponse(
                    id = transition.transitionedBy.id,
                    name = transition.transitionedBy.name,
                    email = transition.transitionedBy.email,
                    role = transition.transitionedBy.role
                )
            )
        }
    }
}
