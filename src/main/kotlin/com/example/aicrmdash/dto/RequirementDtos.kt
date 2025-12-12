package com.example.aicrmdash.dto

import com.example.aicrmdash.domain.Phase
import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * Response DTO for requirement information.
 */
data class RequirementResponse(
    @Schema(description = "Requirement ID", example = "1")
    val id: Long,
    
    @Schema(description = "POC ID this requirement belongs to", example = "1")
    val pocId: Long,
    
    @Schema(description = "Phase this requirement belongs to", example = "DISCOVERY")
    val phase: Phase,
    
    @Schema(description = "Requirement description", example = "Define success criteria")
    val description: String,
    
    @Schema(description = "Whether the requirement is completed", example = "false")
    val completed: Boolean,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Completion timestamp", example = "2025-11-19T15:30:00")
    val completedAt: LocalDateTime?,
    
    @Schema(description = "Additional notes", example = "Discussed with customer")
    val notes: String?,
    
    @Schema(description = "Display order", example = "1")
    val displayOrder: Int
)

/**
 * Request DTO for updating a requirement.
 */
data class UpdateRequirementRequest(
    @Schema(description = "Mark requirement as completed or not", example = "true")
    val completed: Boolean? = null,
    
    @field:Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Schema(description = "Additional notes", example = "Completed after customer approval")
    val notes: String? = null
)
