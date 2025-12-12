package com.example.aicrmdash.controller

import com.example.aicrmdash.dto.RequirementResponse
import com.example.aicrmdash.dto.UpdateRequirementRequest
import com.example.aicrmdash.service.RequirementService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for POC requirement management.
 *
 * Provides endpoints for viewing and updating requirements for POC phases.
 */
@RestController
@RequestMapping("/api/v1/pocs/{pocId}/requirements")
@Tag(name = "Requirements", description = "APIs for managing POC phase requirements")
class RequirementController(
    private val requirementService: RequirementService
) {

    /**
     * Lists all requirements for a POC's current phase.
     *
     * @param pocId The POC ID
     * @return List of requirements ordered by display order
     */
    @Operation(
        summary = "Get requirements for POC",
        description = "Retrieves all requirements for the POC's current phase"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved requirements"),
            ApiResponse(responseCode = "404", description = "POC not found")
        ]
    )
    @GetMapping
    fun getRequirements(@Parameter(description = "POC ID") @PathVariable pocId: Long): ResponseEntity<List<RequirementResponse>> {
        val requirements = requirementService.getRequirementsForPoc(pocId)
        val responses = requirements.map { requirement ->
            RequirementResponse(
                id = requirement.id,
                pocId = requirement.pocId,
                phase = requirement.phase,
                description = requirement.description,
                completed = requirement.completed,
                completedAt = requirement.completedAt,
                notes = requirement.notes,
                displayOrder = requirement.displayOrder
            )
        }
        return ResponseEntity.ok(responses)
    }

    /**
     * Updates a requirement's completion status and/or notes.
     *
     * @param pocId The POC ID
     * @param requirementId The requirement ID to update
     * @param request The update request containing optional completed status and notes
     * @return Updated requirement information
     */
    @Operation(
        summary = "Update requirement",
        description = "Updates a requirement's completion status and/or notes"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Requirement updated successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request or validation error"),
            ApiResponse(responseCode = "404", description = "POC or requirement not found")
        ]
    )
    @PatchMapping("/{requirementId}")
    fun updateRequirement(
        @Parameter(description = "POC ID") @PathVariable pocId: Long,
        @Parameter(description = "Requirement ID") @PathVariable requirementId: Long,
        @Valid @RequestBody request: UpdateRequirementRequest
    ): ResponseEntity<RequirementResponse> {
        val requirement = requirementService.updateRequirement(
            pocId = pocId,
            requirementId = requirementId,
            completed = request.completed,
            notes = request.notes
        )
        val response = RequirementResponse(
            id = requirement.id,
            pocId = requirement.pocId,
            phase = requirement.phase,
            description = requirement.description,
            completed = requirement.completed,
            completedAt = requirement.completedAt,
            notes = requirement.notes,
            displayOrder = requirement.displayOrder
        )
        return ResponseEntity.ok(response)
    }
}
