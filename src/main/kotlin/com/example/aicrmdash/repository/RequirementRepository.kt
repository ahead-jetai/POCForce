package com.example.aicrmdash.repository

import com.example.aicrmdash.domain.Phase
import com.example.aicrmdash.domain.Requirement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository interface for Requirement entity operations.
 *
 * Provides standard CRUD operations and custom query methods for Requirement entities.
 */
@Repository
interface RequirementRepository : JpaRepository<Requirement, Long> {
    
    /**
     * Finds all requirements for a specific POC.
     *
     * @param pocId The POC ID
     * @return List of requirements ordered by display order
     */
    fun findByPocIdOrderByDisplayOrder(pocId: Long): List<Requirement>
    
    /**
     * Finds all requirements for a specific POC and phase.
     *
     * @param pocId The POC ID
     * @param phase The phase
     * @return List of requirements ordered by display order
     */
    fun findByPocIdAndPhaseOrderByDisplayOrder(pocId: Long, phase: Phase): List<Requirement>
    
    /**
     * Deletes all requirements for a specific POC.
     *
     * @param pocId The POC ID
     */
    fun deleteByPocId(pocId: Long)
}
