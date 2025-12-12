package com.example.aicrmdash.domain

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * Represents a requirement or checklist item for a specific phase of a POC engagement.
 *
 * Requirements track the specific tasks or validation criteria that must be completed
 * before a POC can advance to the next phase.
 */
@Entity
@Table(name = "requirements")
data class Requirement(
    /**
     * Unique identifier for the requirement.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /**
     * Foreign key reference to the POC this requirement belongs to.
     */
    @Column(nullable = false, insertable = false, updatable = false)
    val pocId: Long,

    /**
     * The phase this requirement applies to.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val phase: Phase,

    /**
     * Description of what needs to be completed for this requirement.
     */
    @Column(nullable = false, length = 500)
    @field:NotBlank(message = "Description is required")
    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    val description: String,

    /**
     * Whether this requirement has been completed.
     */
    @Column(nullable = false)
    val completed: Boolean = false,

    /**
     * Timestamp when this requirement was marked as completed.
     * Null if not yet completed.
     */
    @Column
    val completedAt: LocalDateTime? = null,

    /**
     * Optional notes or additional information about this requirement.
     */
    @Column(length = 1000)
    @field:Size(max = 1000, message = "Notes must not exceed 1000 characters")
    val notes: String? = null,

    /**
     * Display order for this requirement within its phase.
     * Lower numbers are displayed first.
     */
    @Column(nullable = false)
    val displayOrder: Int
)
