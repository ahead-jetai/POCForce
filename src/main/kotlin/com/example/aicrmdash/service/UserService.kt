package com.example.aicrmdash.service

import com.example.aicrmdash.domain.User
import com.example.aicrmdash.exception.ResourceNotFoundException
import com.example.aicrmdash.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service layer for user management operations.
 *
 * Handles user retrieval and lookup.
 */
@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    /**
     * Retrieves all users in the system.
     *
     * @return List of all users
     */
    fun getAllUsers(): List<User> {
        logger.info("Retrieving all users")
        val users = userRepository.findAll()
        logger.info("Retrieved {} users", users.size)
        return users
    }

    /**
     * Retrieves a user by ID.
     *
     * @param id The user ID
     * @return The user entity
     * @throws ResourceNotFoundException if user not found
     */
    fun getUserById(id: Long): User {
        logger.info("Retrieving user by ID: {}", id)
        val user = userRepository.findById(id)
            .orElseThrow { 
                logger.warn("User not found with ID: {}", id)
                ResourceNotFoundException("User with ID $id not found") 
            }
        logger.info("User retrieved successfully: id={}, name={}", user.id, user.name)
        return user
    }
}
