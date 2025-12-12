package com.example.aicrmdash.service

import com.example.aicrmdash.domain.Phase
import com.example.aicrmdash.domain.Status
import com.example.aicrmdash.repository.PocRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * Service layer for dashboard metrics and summary data.
 *
 * Provides aggregated statistics and insights for POC management.
 */
@Service
@Transactional(readOnly = true)
class DashboardService(
    private val pocRepository: PocRepository,
    private val pocService: PocService
) {

    private val logger = LoggerFactory.getLogger(DashboardService::class.java)

    /**
     * Calculates summary metrics for the dashboard.
     *
     * Implements FR-30, FR-31, FR-32:
     * - Active POC count (ACTIVE or AT_RISK status)
     * - At-risk POC count (AT_RISK status)
     * - Total pipeline value (sum of dealValue for active POCs)
     * - POCs by phase (count for each phase)
     *
     * @return Dashboard summary data
     */
    fun getSummaryMetrics(): DashboardSummary {
        logger.info("Calculating dashboard summary metrics")
        
        // Get all POCs and recalculate their statuses
        val allPocs = pocRepository.findAll()
        allPocs.forEach { pocService.calculateStatus(it) }
        
        // Reload POCs to get updated statuses
        val pocs = pocRepository.findAll()
        
        // Calculate active POC count (ACTIVE or AT_RISK)
        val activePocCount = pocs.count { it.status == Status.ACTIVE || it.status == Status.AT_RISK }
        
        // Calculate at-risk POC count
        val atRiskPocCount = pocs.count { it.status == Status.AT_RISK }
        
        // Calculate total pipeline value for active POCs
        val totalPipelineValue = pocs
            .filter { it.status == Status.ACTIVE || it.status == Status.AT_RISK }
            .map { it.dealValue }
            .fold(BigDecimal.ZERO) { sum, value -> sum + value }
        
        // Calculate POCs by phase
        val pocsByPhase = Phase.entries.associateWith { phase ->
            pocs.count { it.currentPhase == phase }
        }
        
        logger.info("Dashboard metrics calculated: activePocs={}, atRiskPocs={}, pipelineValue={}", 
            activePocCount, atRiskPocCount, totalPipelineValue)
        
        return DashboardSummary(
            activePocCount = activePocCount,
            atRiskPocCount = atRiskPocCount,
            totalPipelineValue = totalPipelineValue,
            pocsByPhase = pocsByPhase
        )
    }
}

/**
 * Data class holding dashboard summary metrics.
 */
data class DashboardSummary(
    val activePocCount: Int,
    val atRiskPocCount: Int,
    val totalPipelineValue: BigDecimal,
    val pocsByPhase: Map<Phase, Int>
)
