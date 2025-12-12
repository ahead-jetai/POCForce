package com.example.aicrmdash.domain

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * PhaseTransition entity represents a single phase transition in a POC's lifecycle.
 * This provides an audit trail of when and how a POC moved between phases.
 *
 * @property id Unique identifier for the phase transition
 * @property pocId Foreign key to the POC that transitioned
 * @property fromPhase The phase the POC transitioned from (null for initial phase)
 * @property toPhase The phase the POC transitioned to
 * @property transitionedAt Timestamp when the transition occurred
 * @property transitionedBy User who initiated the phase transition
 */
@Entity
@Table(name = "phase_transitions")
data class PhaseTransition(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val pocId: Long,

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    val fromPhase: Phase?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val toPhase: Phase,

    @Column(nullable = false)
    val transitionedAt: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transitioned_by", nullable = false)
    val transitionedBy: User
)
