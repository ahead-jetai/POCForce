package com.example.aicrmdash.repository

import com.example.aicrmdash.domain.Poc
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Repository interface for Poc entity operations.
 *
 * Provides standard CRUD operations and custom query methods for POC entities.
 */
@Repository
interface PocRepository : JpaRepository<Poc, Long> {

    /**
     * Finds a POC by ID with owner eagerly fetched.
     */
    @Query("SELECT p FROM Poc p LEFT JOIN FETCH p.owner WHERE p.id = :id")
    fun findByIdWithOwner(id: Long): Optional<Poc>
}
