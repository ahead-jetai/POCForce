package com.example.aicrmdash.config

import com.example.aicrmdash.domain.Phase
import com.example.aicrmdash.domain.User
import com.example.aicrmdash.domain.UserRole
import com.example.aicrmdash.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * Configuration class for initializing database with seed data.
 *
 * This initializer runs on application startup and creates:
 * - Default users for testing and development
 * - Default requirement templates for each POC phase (stored as data, not in DB)
 */
@Configuration
class DataInitializer {

    /**
     * CommandLineRunner bean that seeds the database with initial users.
     * 
     * Default password for all seed users is "password123" (hashed with BCrypt).
     */
    @Bean
    fun initializeUsers(userRepository: UserRepository, passwordEncoder: PasswordEncoder): CommandLineRunner {
        return CommandLineRunner {
            if (userRepository.count() == 0L) {
                val users = listOf(
                    User(
                        name = "Arthur Head",
                        email = "arthur.head@example.com",
                        password = passwordEncoder.encode("test1234"),
                        role = UserRole.SE
                    ),
                    User(
                        name = "Bob Smith",
                        email = "bob@example.com",
                        password = passwordEncoder.encode("password123"),
                        role = UserRole.CSE
                    ),
                    User(
                        name = "Carol Davis",
                        email = "carol@example.com",
                        password = passwordEncoder.encode("password123"),
                        role = UserRole.SALES_ENGINEER
                    )
                )
                userRepository.saveAll(users)
                println("Initialized ${users.size} seed users")
            }
        }
    }
}

/**
 * Object holding default requirement templates for each POC phase.
 *
 * These templates are used when creating requirements for a new POC or when advancing to a new phase.
 */
object DefaultRequirements {
    
    private val requirementsByPhase = mapOf(
        Phase.DISCOVERY to listOf(
            "Identify key stakeholders and decision makers",
            "Document technical requirements and success criteria",
            "Assess existing infrastructure and integration points",
            "Define scope and boundaries of POC",
            "Establish communication plan and meeting cadence"
        ),
        Phase.PLANNING to listOf(
            "Create detailed POC timeline with milestones",
            "Define test scenarios and validation approach",
            "Identify required resources and access",
            "Document risks and mitigation strategies",
            "Get stakeholder sign-off on POC plan"
        ),
        Phase.EXECUTION to listOf(
            "Set up POC environment",
            "Implement core use cases",
            "Conduct technical validation tests",
            "Gather stakeholder feedback",
            "Document any blockers or issues"
        ),
        Phase.VALIDATION to listOf(
            "Execute final validation against success criteria",
            "Gather feedback from all stakeholders",
            "Document lessons learned and outcomes",
            "Prepare executive summary and recommendations",
            "Obtain formal sign-off on POC results"
        )
    )

    /**
     * Retrieves the list of default requirement descriptions for a given phase.
     *
     * @param phase The POC phase to get requirements for
     * @return List of requirement descriptions, or empty list if no defaults exist for the phase
     */
    fun getRequirementsForPhase(phase: Phase): List<String> {
        return requirementsByPhase[phase] ?: emptyList()
    }
}
