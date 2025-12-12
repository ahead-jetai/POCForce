package com.example.aicrmdash.exception

/**
 * Exception thrown when a requested resource is not found.
 *
 * This exception typically results in an HTTP 404 (Not Found) response.
 *
 * @param message Description of what resource was not found
 */
class ResourceNotFoundException(message: String) : RuntimeException(message)

/**
 * Exception thrown when a business rule validation fails.
 *
 * This exception typically results in an HTTP 400 (Bad Request) response.
 * Use this for domain-specific validation errors that are not caught by bean validation.
 *
 * @param message Description of the validation failure
 */
class ValidationException(message: String) : RuntimeException(message)

/**
 * Exception thrown when an invalid phase transition is attempted.
 *
 * This exception typically results in an HTTP 400 (Bad Request) response.
 * Use this when:
 * - Attempting to advance a phase when requirements are incomplete
 * - Attempting to transition from a terminal phase
 * - Attempting other invalid phase state changes
 *
 * @param message Description of why the phase transition is invalid
 * @param incompleteRequirements Optional list of incomplete requirement descriptions
 */
class PhaseTransitionException(
    message: String,
    val incompleteRequirements: List<String> = emptyList()
) : RuntimeException(message)
