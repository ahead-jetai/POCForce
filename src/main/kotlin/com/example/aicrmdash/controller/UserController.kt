package com.example.aicrmdash.controller

import com.example.aicrmdash.dto.UserResponse
import com.example.aicrmdash.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for user management operations.
 *
 * Provides endpoints for retrieving user information.
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "APIs for user management")
class UserController(
    private val userService: UserService
) {

    /**
     * Lists all users in the system.
     *
     * @return List of all users
     */
    @Operation(summary = "Get all users", description = "Retrieves a list of all users in the system")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved users")
        ]
    )
    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        val users = userService.getAllUsers()
        val responses = users.map { user ->
            UserResponse(
                id = user.id,
                name = user.name,
                email = user.email,
                role = user.role
            )
        }
        return ResponseEntity.ok(responses)
    }

    /**
     * Retrieves a single user by ID.
     *
     * @param id The user ID
     * @return User information
     */
    @Operation(summary = "Get user by ID", description = "Retrieves detailed information about a specific user")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "User found"),
            ApiResponse(responseCode = "404", description = "User not found")
        ]
    )
    @GetMapping("/{id}")
    fun getUserById(@Parameter(description = "User ID") @PathVariable id: Long): ResponseEntity<UserResponse> {
        val user = userService.getUserById(id)
        val response = UserResponse(
            id = user.id,
            name = user.name,
            email = user.email,
            role = user.role
        )
        return ResponseEntity.ok(response)
    }
}
