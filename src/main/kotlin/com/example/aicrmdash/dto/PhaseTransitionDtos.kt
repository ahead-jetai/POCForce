package com.example.aicrmdash.dto

import com.example.aicrmdash.domain.Phase
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * Response DTO for phase transition information.
 * Represents a single phase transition in a POC's audit trail.
 */
data class PhaseTransitionResponse(
    @Schema(description = "Phase transition ID", example = "1")
    val id: Long,
    
    @Schema(description = "POC ID", example = "1")
    val pocId: Long,
    
    @Schema(description = "Phase transitioned from (null for initial phase)", example = "DISCOVERY", nullable = true)
    val fromPhase: Phase?,
    
    @Schema(description = "Phase transitioned to", example = "PLANNING")
    val toPhase: Phase,
    
    @Schema(description = "Timestamp when the transition occurred", example = "2025-11-19T23:30:00")
    val transitionedAt: LocalDateTime,
    
    @Schema(description = "User who initiated the phase transition")
    val transitionedBy: UserResponse
)
