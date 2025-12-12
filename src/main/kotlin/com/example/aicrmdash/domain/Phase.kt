package com.example.aicrmdash.domain

/**
 * Represents the lifecycle phases of a POC engagement.
 *
 * POCs progress through phases sequentially from DISCOVERY to either CLOSED_WON or CLOSED_LOST.
 * Once a POC reaches a terminal phase (CLOSED_WON or CLOSED_LOST), no further transitions are allowed.
 */
enum class Phase {
    /**
     * Initial phase where technical requirements and success criteria are identified.
     */
    DISCOVERY,

    /**
     * Phase where POC approach, timeline, and resources are planned.
     */
    PLANNING,

    /**
     * Phase where the POC is actively being executed and validated.
     */
    EXECUTION,

    /**
     * Phase where results are reviewed and success criteria are validated.
     */
    VALIDATION,

    /**
     * Terminal phase indicating the POC was successful and deal was won.
     */
    CLOSED_WON,

    /**
     * Terminal phase indicating the POC did not result in a deal.
     */
    CLOSED_LOST;

    /**
     * Checks if this phase is a terminal phase (no further transitions allowed).
     *
     * @return true if this phase is CLOSED_WON or CLOSED_LOST
     */
    fun isTerminal(): Boolean {
        return this == CLOSED_WON || this == CLOSED_LOST
    }

    /**
     * Returns the next phase in the sequence, or null if this is a terminal phase.
     *
     * @return the next Phase in the sequence, or null if terminal or last non-terminal phase
     */
    fun nextPhase(): Phase? {
        return when (this) {
            DISCOVERY -> PLANNING
            PLANNING -> EXECUTION
            EXECUTION -> VALIDATION
            VALIDATION -> null // Must explicitly close as WON or LOST
            CLOSED_WON, CLOSED_LOST -> null // Terminal phases have no next phase
        }
    }
}
