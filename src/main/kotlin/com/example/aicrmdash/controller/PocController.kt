package com.example.aicrmdash.controller

import com.example.aicrmdash.domain.Phase
import com.example.aicrmdash.domain.Status
import com.example.aicrmdash.dto.*
import com.example.aicrmdash.exception.ResourceNotFoundException
import com.example.aicrmdash.service.PocService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

/**
 * REST controller for POC management operations.
 *
 * Provides endpoints for creating, retrieving, updating, and deleting POCs.
 */
@RestController
@RequestMapping("/api/v1/pocs")
@Tag(name = "POC Management", description = "APIs for managing POCs (Proof of Concepts)")
class PocController(
    private val pocService: PocService
) {

    /**
     * Converts a POC domain model to a PocDetailResponse DTO.
     */
    private fun toPocDetailResponse(poc: com.example.aicrmdash.domain.Poc): PocDetailResponse {
        val owner = poc.owner ?: throw ResourceNotFoundException("Owner not found for POC")
        return PocDetailResponse(
            id = poc.id,
            customerName = poc.customerName,
            title = poc.title,
            description = poc.description,
            dealValue = poc.dealValue,
            projectedCloseDate = poc.projectedCloseDate,
            kickoffDate = poc.kickoffDate,
            endDate = poc.endDate,
            currentPhase = poc.currentPhase,
            status = poc.status,
            ownerId = poc.ownerId,
            owner = UserResponse(
                id = owner.id,
                name = owner.name,
                email = owner.email,
                role = owner.role
            ),
            createdAt = poc.createdAt,
            updatedAt = poc.updatedAt
        )
    }

    /**
     * Lists all POCs with optional filtering and search.
     *
     * @param phase Optional filter by phase
     * @param ownerId Optional filter by owner ID
     * @param status Optional filter by status
     * @param search Optional search term for customer name or title
     * @return List of POC summaries
     */
    @Operation(
        summary = "List all POCs",
        description = "Retrieves a list of all POCs with optional filtering by phase, owner, status, and search query"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved POCs")
        ]
    )
    @GetMapping
    fun listPocs(
        @Parameter(description = "Filter by phase") @RequestParam(required = false) phase: Phase?,
        @Parameter(description = "Filter by owner ID") @RequestParam(required = false) ownerId: Long?,
        @Parameter(description = "Filter by status") @RequestParam(required = false) status: Status?,
        @Parameter(description = "Search in customer name or title") @RequestParam(required = false) search: String?
    ): ResponseEntity<List<PocSummaryResponse>> {
        val pocs = pocService.filterPocs(phase, ownerId, status, search)
        val responses = pocs.map { poc ->
            PocSummaryResponse(
                id = poc.id,
                customerName = poc.customerName,
                title = poc.title,
                dealValue = poc.dealValue,
                projectedCloseDate = poc.projectedCloseDate,
                kickoffDate = poc.kickoffDate,
                endDate = poc.endDate,
                currentPhase = poc.currentPhase,
                status = poc.status,
                ownerId = poc.ownerId,
                ownerName = poc.owner?.name
            )
        }
        return ResponseEntity.ok(responses)
    }

    /**
     * Retrieves a single POC by ID.
     *
     * @param id The POC ID
     * @return Detailed POC information
     */
    @Operation(summary = "Get POC by ID", description = "Retrieves detailed information about a specific POC")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "POC found"),
            ApiResponse(responseCode = "404", description = "POC not found")
        ]
    )
    @GetMapping("/{id}")
    fun getPocById(@Parameter(description = "POC ID") @PathVariable id: Long): ResponseEntity<PocDetailResponse> {
        val poc = pocService.getPocById(id)
        return ResponseEntity.ok(toPocDetailResponse(poc))
    }

    /**
     * Creates a new POC.
     *
     * @param request The POC creation request
     * @return The created POC details with Location header
     */
    @Operation(summary = "Create new POC", description = "Creates a new POC with initial phase set to DISCOVERY")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "POC created successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request data")
        ]
    )
    @PostMapping
    fun createPoc(@Valid @RequestBody request: CreatePocRequest): ResponseEntity<PocDetailResponse> {
        val poc = pocService.createPoc(request)
        val response = toPocDetailResponse(poc)
        val location = URI.create("/api/v1/pocs/${poc.id}")
        return ResponseEntity.created(location).body(response)
    }

    /**
     * Updates an existing POC.
     *
     * @param id The POC ID
     * @param request The update request
     * @return The updated POC details
     */
    @Operation(summary = "Update POC", description = "Updates an existing POC. Cannot update POCs in terminal phases.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "POC updated successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request or POC in terminal phase"),
            ApiResponse(responseCode = "404", description = "POC not found")
        ]
    )
    @PutMapping("/{id}")
    fun updatePoc(
        @Parameter(description = "POC ID") @PathVariable id: Long,
        @Valid @RequestBody request: UpdatePocRequest
    ): ResponseEntity<PocDetailResponse> {
        val poc = pocService.updatePoc(id, request)
        return ResponseEntity.ok(toPocDetailResponse(poc))
    }

    /**
     * Deletes a POC by ID.
     *
     * @param id The POC ID
     * @return No content response
     */
    @Operation(summary = "Delete POC", description = "Deletes a POC and all associated requirements")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "POC deleted successfully"),
            ApiResponse(responseCode = "404", description = "POC not found")
        ]
    )
    @DeleteMapping("/{id}")
    fun deletePoc(@Parameter(description = "POC ID") @PathVariable id: Long): ResponseEntity<Void> {
        pocService.deletePoc(id)
        return ResponseEntity.noContent().build()
    }

    /**
     * Advances a POC to the next phase.
     *
     * Validates that all current phase requirements are complete before advancing.
     *
     * @param id The POC ID
     * @return Updated POC details
     */
    @Operation(
        summary = "Advance POC phase", 
        description = "Advances POC to the next phase. All current phase requirements must be complete."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Phase advanced successfully"),
            ApiResponse(responseCode = "400", description = "Requirements incomplete or POC in terminal phase"),
            ApiResponse(responseCode = "404", description = "POC not found")
        ]
    )
    @PostMapping("/{id}/advance")
    fun advancePhase(@Parameter(description = "POC ID") @PathVariable id: Long): ResponseEntity<PocDetailResponse> {
        val poc = pocService.advancePhase(id)
        return ResponseEntity.ok(toPocDetailResponse(poc))
    }

    /**
     * Closes a POC with a final outcome (WON or LOST).
     *
     * @param id The POC ID
     * @param request The closure request with outcome and optional notes
     * @return Updated POC details
     */
    @Operation(
        summary = "Close POC", 
        description = "Closes a POC with final outcome (WON or LOST). Sets phase to CLOSED_WON or CLOSED_LOST."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "POC closed successfully"),
            ApiResponse(responseCode = "400", description = "Invalid outcome"),
            ApiResponse(responseCode = "404", description = "POC not found")
        ]
    )
    @PostMapping("/{id}/close")
    fun closePoc(
        @Parameter(description = "POC ID") @PathVariable id: Long,
        @Valid @RequestBody request: ClosePocRequest
    ): ResponseEntity<PocDetailResponse> {
        val poc = pocService.closePoc(id, request.outcome, request.notes)
        return ResponseEntity.ok(toPocDetailResponse(poc))
    }

    /**
     * Retrieves the phase transition history for a POC.
     *
     * Returns all phase transitions in chronological order, providing an audit trail
     * of how the POC moved through different phases.
     *
     * @param id The POC ID
     * @return List of phase transitions
     */
    @Operation(
        summary = "Get phase transition history",
        description = "Retrieves the complete phase transition history for a POC, ordered chronologically"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Phase transition history retrieved successfully"),
            ApiResponse(responseCode = "404", description = "POC not found")
        ]
    )
    @GetMapping("/{id}/phase-transitions")
    fun getPhaseTransitionHistory(
        @Parameter(description = "POC ID") @PathVariable id: Long
    ): ResponseEntity<List<PhaseTransitionResponse>> {
        val transitions = pocService.getPhaseTransitionHistory(id)
        return ResponseEntity.ok(transitions)
    }
}
