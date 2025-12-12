package com.example.aicrmdash.domain

/**
 * Represents the role of a user in the organization.
 *
 * Different roles have different responsibilities in managing POC engagements.
 */
enum class UserRole {
    /**
     * Solutions Engineer - Technical sales role responsible for POC execution and technical validation.
     */
    SE,

    /**
     * Customer Success Engineer - Post-sales technical role, sometimes involved in pre-sale POCs.
     */
    CSE,

    /**
     * Sales Engineer - Hybrid technical-sales role focused on POC management and business metrics.
     */
    SALES_ENGINEER,

    /**
     * Manager - Leadership role with oversight of POC pipeline and team performance.
     */
    MANAGER
}
