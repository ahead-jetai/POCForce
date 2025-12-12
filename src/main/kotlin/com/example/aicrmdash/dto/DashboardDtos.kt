package com.example.aicrmdash.dto

import com.example.aicrmdash.domain.Phase
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

/**
 * Response DTO for dashboard summary metrics.
 */
data class DashboardSummaryResponse(
    @Schema(description = "Number of active POCs", example = "5")
    val activePocCount: Int,
    
    @Schema(description = "Number of at-risk POCs", example = "2")
    val atRiskPocCount: Int,
    
    @Schema(description = "Total pipeline value in USD", example = "250000.00")
    val totalPipelineValue: BigDecimal,
    
    @Schema(description = "Number of POCs grouped by phase", example = "{\"DISCOVERY\": 2, \"PLANNING\": 1, \"EXECUTION\": 2}")
    val pocsByPhase: Map<Phase, Int>
)
