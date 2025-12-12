package com.example.aicrmdash.repository

import com.example.aicrmdash.domain.PhaseTransition
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository interface for PhaseTransition entity.
 * Provides database access methods for phase transition audit trail.
 */
@Repository
interface PhaseTransitionRepository : JpaRepository<PhaseTransition, Long> {
    
    /**
     * Find all phase transitions for a specific POC, ordered by transition timestamp.
     *
     * @param pocId The ID of the POC
     * @return List of phase transitions ordered by transitionedAt ascending
     */
    fun findByPocIdOrderByTransitionedAtAsc(pocId: Long): List<PhaseTransition>
}
