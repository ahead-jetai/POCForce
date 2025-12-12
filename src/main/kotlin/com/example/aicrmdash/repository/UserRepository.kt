package com.example.aicrmdash.repository

import com.example.aicrmdash.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository interface for User entity operations.
 *
 * Provides standard CRUD operations and custom query methods for User entities.
 */
@Repository
interface UserRepository : JpaRepository<User, Long> {
    
    /**
     * Finds a user by email address.
     *
     * @param email The email address to search for
     * @return User if found, null otherwise
     */
    fun findByEmail(email: String): User?
}
