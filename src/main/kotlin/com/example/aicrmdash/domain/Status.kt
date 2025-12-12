package com.example.aicrmdash.domain

/**
 * Represents the operational status of a POC engagement.
 *
 * Status is calculated based on the POC's current phase, end date, and progress.
 */
enum class Status {
    /**
     * POC is progressing normally and on track.
     *
     * Applied when:
     * - POC is in a non-terminal phase (not CLOSED_WON or CLOSED_LOST)
     * - Current date is on or before the POC end date
     */
    ACTIVE,

    /**
     * POC is at risk and requires attention.
     *
     * Applied when:
     * - POC is in a non-terminal phase (not CLOSED_WON or CLOSED_LOST)
     * - Current date has passed the POC end date
     */
    AT_RISK,

    /**
     * POC has reached a terminal state.
     *
     * Applied when:
     * - POC phase is CLOSED_WON or CLOSED_LOST
     */
    CLOSED
}
