package com.example.aicrmdash.dto

import com.example.aicrmdash.domain.Phase
import com.example.aicrmdash.domain.Status
import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Request DTO for creating a new POC.
 */
data class CreatePocRequest(
    @field:NotBlank(message = "Customer name is required")
    @field:Size(max = 200, message = "Customer name must not exceed 200 characters")
    @Schema(description = "Name of the customer", example = "Acme Corp")
    val customerName: String,

    @field:NotBlank(message = "Title is required")
    @field:Size(max = 200, message = "Title must not exceed 200 characters")
    @Schema(description = "POC title", example = "Q1 Performance Testing POC")
    val title: String,

    @field:Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(description = "Detailed description of the POC", example = "Load testing for 10K concurrent users")
    val description: String? = null,

    @field:NotNull(message = "Deal value is required")
    @field:Positive(message = "Deal value must be positive")
    @Schema(description = "Deal value in USD", example = "50000.00")
    val dealValue: BigDecimal,

    @field:NotNull(message = "Projected close date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Projected close date", example = "2025-12-31")
    val projectedCloseDate: LocalDate,

    @field:NotNull(message = "Kickoff date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "POC kickoff date", example = "2025-11-20")
    val kickoffDate: LocalDate,

    @field:NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "POC end date", example = "2025-12-15")
    val endDate: LocalDate,

    @field:NotNull(message = "Owner ID is required")
    @Schema(description = "ID of the user owning this POC", example = "1")
    val ownerId: Long
)

/**
 * Request DTO for updating an existing POC.
 */
data class UpdatePocRequest(
    @field:NotBlank(message = "Customer name is required")
    @field:Size(max = 200, message = "Customer name must not exceed 200 characters")
    @Schema(description = "Name of the customer", example = "Acme Corp")
    val customerName: String,

    @field:NotBlank(message = "Title is required")
    @field:Size(max = 200, message = "Title must not exceed 200 characters")
    @Schema(description = "POC title", example = "Q1 Performance Testing POC")
    val title: String,

    @field:Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(description = "Detailed description of the POC", example = "Load testing for 10K concurrent users")
    val description: String? = null,

    @field:NotNull(message = "Deal value is required")
    @field:Positive(message = "Deal value must be positive")
    @Schema(description = "Deal value in USD", example = "50000.00")
    val dealValue: BigDecimal,

    @field:NotNull(message = "Projected close date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Projected close date", example = "2025-12-31")
    val projectedCloseDate: LocalDate,

    @field:NotNull(message = "Kickoff date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "POC kickoff date", example = "2025-11-20")
    val kickoffDate: LocalDate,

    @field:NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "POC end date", example = "2025-12-15")
    val endDate: LocalDate,

    @field:NotNull(message = "Owner ID is required")
    @Schema(description = "ID of the user owning this POC", example = "1")
    val ownerId: Long
)

/**
 * Response DTO for POC summary in list views.
 */
data class PocSummaryResponse(
    @Schema(description = "POC ID", example = "1")
    val id: Long,
    
    @Schema(description = "Customer name", example = "Acme Corp")
    val customerName: String,
    
    @Schema(description = "POC title", example = "Q1 Performance Testing POC")
    val title: String,
    
    @Schema(description = "Deal value in USD", example = "50000.00")
    val dealValue: BigDecimal,
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Projected close date", example = "2025-12-31")
    val projectedCloseDate: LocalDate,
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "POC kickoff date", example = "2025-11-20")
    val kickoffDate: LocalDate,
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "POC end date", example = "2025-12-15")
    val endDate: LocalDate,
    
    @Schema(description = "Current phase", example = "DISCOVERY")
    val currentPhase: Phase,
    
    @Schema(description = "POC status", example = "ACTIVE")
    val status: Status,
    
    @Schema(description = "Owner user ID", example = "1")
    val ownerId: Long,
    
    @Schema(description = "Owner user name", example = "John Doe")
    val ownerName: String?
)

/**
 * Response DTO for detailed POC view.
 */
data class PocDetailResponse(
    @Schema(description = "POC ID", example = "1")
    val id: Long,

    @Schema(description = "Customer name", example = "Acme Corp")
    val customerName: String,

    @Schema(description = "POC title", example = "Q1 Performance Testing POC")
    val title: String,

    @Schema(description = "Detailed description", example = "Load testing for 10K concurrent users")
    val description: String?,

    @Schema(description = "Deal value in USD", example = "50000.00")
    val dealValue: BigDecimal,

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Projected close date", example = "2025-12-31")
    val projectedCloseDate: LocalDate,

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "POC kickoff date", example = "2025-11-20")
    val kickoffDate: LocalDate,

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "POC end date", example = "2025-12-15")
    val endDate: LocalDate,

    @Schema(description = "Current phase", example = "DISCOVERY")
    val currentPhase: Phase,

    @Schema(description = "POC status", example = "ACTIVE")
    val status: Status,

    @Schema(description = "Owner user ID", example = "1")
    val ownerId: Long,

    @Schema(description = "Owner details")
    val owner: UserResponse,

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Creation timestamp", example = "2025-11-19T10:30:00")
    val createdAt: LocalDateTime,

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Last update timestamp", example = "2025-11-19T14:45:00")
    val updatedAt: LocalDateTime
)

/**
 * Request DTO for closing a POC.
 */
data class ClosePocRequest(
    @field:NotBlank(message = "Outcome is required")
    @field:Pattern(regexp = "WON|LOST", message = "Outcome must be either WON or LOST")
    @Schema(description = "Outcome of the POC", example = "WON", allowableValues = ["WON", "LOST"])
    val outcome: String,

    @field:Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Schema(description = "Closing notes", example = "Customer signed the contract")
    val notes: String? = null
)
