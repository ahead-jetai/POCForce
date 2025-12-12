package com.example.aicrmdash.domain

import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Represents a Proof of Concept (POC) engagement.
 *
 * POCs track technical validation projects through defined lifecycle phases,
 * from initial discovery through to successful close or loss.
 */
@Entity
@Table(name = "pocs")
data class Poc(
    /**
     * Unique identifier for the POC.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /**
     * Name of the customer/company for this POC.
     */
    @Column(nullable = false, length = 200)
    @field:NotBlank(message = "Customer name is required")
    @field:Size(max = 200, message = "Customer name must not exceed 200 characters")
    val customerName: String,

    /**
     * Title or short description of the POC.
     */
    @Column(nullable = false, length = 200)
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 200, message = "Title must not exceed 200 characters")
    val title: String,

    /**
     * Detailed description of the POC objectives and scope.
     */
    @Column(length = 2000)
    @field:Size(max = 2000, message = "Description must not exceed 2000 characters")
    val description: String? = null,

    /**
     * Projected deal value in dollars.
     */
    @Column(nullable = false, precision = 15, scale = 2)
    @field:NotNull(message = "Deal value is required")
    @field:Positive(message = "Deal value must be positive")
    val dealValue: BigDecimal,

    /**
     * Expected close date for the deal.
     */
    @Column(nullable = false)
    @field:NotNull(message = "Projected close date is required")
    val projectedCloseDate: LocalDate,

    /**
     * Date when the POC officially starts.
     */
    @Column(nullable = false)
    @field:NotNull(message = "Kickoff date is required")
    val kickoffDate: LocalDate,

    /**
     * Date when the POC is scheduled to end.
     * Must be on or after kickoffDate.
     */
    @Column(nullable = false)
    @field:NotNull(message = "End date is required")
    val endDate: LocalDate,

    /**
     * Current phase of the POC lifecycle.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val currentPhase: Phase = Phase.DISCOVERY,

    /**
     * Foreign key reference to the User who owns this POC.
     */
    @Column(nullable = false)
    val ownerId: Long,

    /**
     * Current operational status of the POC.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: Status = Status.ACTIVE,

    /**
     * Timestamp when this POC was created.
     */
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    /**
     * Timestamp when this POC was last updated.
     */
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    /**
     * Relationship to the User who owns this POC.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ownerId", insertable = false, updatable = false)
    val owner: User? = null,

    /**
     * Relationship to the Requirements for this POC.
     * When a POC is deleted, all associated requirements are also deleted.
     */
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "pocId", nullable = false)
    val requirements: MutableList<Requirement> = mutableListOf()
) {
    init {
        require(endDate >= kickoffDate) {
            "End date must be on or after kickoff date"
        }
    }
}
