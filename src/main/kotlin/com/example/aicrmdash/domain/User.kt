package com.example.aicrmdash.domain

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Represents a user in the POCForce system.
 *
 * Users can be Solutions Engineers, Customer Success Engineers, Sales Engineers, or Managers
 * who manage and track POC engagements.
 */
@Entity
@Table(name = "users")
data class User(
    /**
     * Unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /**
     * Full name of the user.
     */
    @Column(nullable = false, length = 100)
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 100, message = "Name must not exceed 100 characters")
    val name: String,

    /**
     * Email address of the user. Must be unique across all users.
     */
    @Column(nullable = false, unique = true, length = 200)
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    @field:Size(max = 200, message = "Email must not exceed 200 characters")
    val email: String,

    /**
     * Encrypted password for authentication. Should be BCrypt hashed.
     */
    @Column(nullable = false)
    @field:NotBlank(message = "Password is required")
    val password: String,

    /**
     * Role of the user in the organization.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole
)
