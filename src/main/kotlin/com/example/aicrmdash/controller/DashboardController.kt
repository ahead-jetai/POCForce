package com.example.aicrmdash.controller

import com.example.aicrmdash.dto.DashboardSummaryResponse
import com.example.aicrmdash.service.DashboardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for dashboard and summary metrics.
 *
 * Provides endpoints for retrieving aggregated POC statistics.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "APIs for dashboard metrics and statistics")
class DashboardController(
    private val dashboardService: DashboardService
) {

    /**
     * Retrieves dashboard summary metrics.
     *
     * Returns aggregated statistics including:
     * - Active POC count
     * - At-risk POC count
     * - Total pipeline value
     * - POCs by phase breakdown
     *
     * @return Dashboard summary metrics
     */
    @Operation(
        summary = "Get dashboard summary",
        description = "Retrieves aggregated metrics including active POCs, at-risk POCs, total pipeline value, and POC count by phase"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved dashboard metrics")
        ]
    )
    @GetMapping("/summary")
    fun getDashboardSummary(): ResponseEntity<DashboardSummaryResponse> {
        val summary = dashboardService.getSummaryMetrics()
        val response = DashboardSummaryResponse(
            activePocCount = summary.activePocCount,
            atRiskPocCount = summary.atRiskPocCount,
            totalPipelineValue = summary.totalPipelineValue,
            pocsByPhase = summary.pocsByPhase
        )
        return ResponseEntity.ok(response)
    }
}
