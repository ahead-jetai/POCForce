# Tasks – POCForce

## Conventions

### Task Naming
- Tasks are identified with unique IDs: **[T-0]**, **[T-1]**, **[T-2]**, etc.
- Each task references the requirement IDs it implements in parentheses: **(FR-1, FR-2, API-7)**

### Task Status
Tasks can have the following statuses:
- **TODO**: Not yet started
- **IN PROGRESS**: Currently being worked on
- **DONE**: Completed and verified
- **BLOCKED**: Waiting on external decision or dependency

### Task Size
- Tasks should be completable in a single focused coding session (2-4 hours)
- If a task is too large, it should be broken down into sub-tasks

### Task Dependencies
- Tasks within a phase should generally be completed in order
- Dependencies on earlier phases are implicit (e.g., Phase 1 depends on Phase 0 completion)

---

## Phase 0 – Foundations / Setup

**Goal**: Set up project infrastructure, dependencies, and foundational domain models.

### [T-0] Add Spring Data JPA and H2 database dependencies
**Status**: DONE  
**Requirements**: NFR-22, NFR-25, DATA-7  
**Description**:
- Add `spring-boot-starter-data-jpa` to build.gradle.kts
- Add H2 database dependency for in-memory development database
- Configure application.properties for H2 console access and JPA settings
- Verify application starts successfully with JPA enabled

---

### [T-1] Add Bean Validation dependency
**Status**: DONE  
**Requirements**: NFR-10, DATA-11, DATA-12  
**Description**:
- Add `spring-boot-starter-validation` to build.gradle.kts
- Verify validation annotations are available for use

---

### [T-2] Create Phase enum
**Status**: DONE  
**Requirements**: FR-11, DATA-14  
**Description**:
- Create `Phase` enum in domain package with values: DISCOVERY, PLANNING, EXECUTION, VALIDATION, CLOSED_WON, CLOSED_LOST
- Add helper methods:
  - `isTerminal(): Boolean` - returns true for CLOSED_WON and CLOSED_LOST
  - `nextPhase(): Phase?` - returns the next phase in sequence, or null if terminal
- Add KDoc comments

---

### [T-3] Create Status enum
**Status**: DONE  
**Requirements**: FR-28, DATA-14  
**Description**:
- Create `Status` enum in domain package with values: ACTIVE, AT_RISK, CLOSED
- Add KDoc comments describing when each status applies

---

### [T-4] Create UserRole enum
**Status**: DONE  
**Requirements**: FR-33, DATA-14  
**Description**:
- Create `UserRole` enum in domain package with values: SE, CSE, SALES_ENGINEER, MANAGER
- Add KDoc comments

---

### [T-5] Create User entity
**Status**: DONE  
**Requirements**: FR-33, DATA-1, DATA-15, NFR-19  
**Description**:
- Create `User` JPA entity with fields:
  - `id: Long` (primary key, auto-generated)
  - `name: String` (max 100 chars, non-null)
  - `email: String` (max 200 chars, unique, non-null)
  - `role: UserRole` (enum, non-null)
- Add validation annotations
- Add KDoc comments
- Create `UserRepository` interface extending JpaRepository

---

### [T-6] Create Requirement entity
**Status**: DONE  
**Requirements**: FR-20, FR-21, FR-22, DATA-5, DATA-6, NFR-19  
**Description**:
- Create `Requirement` JPA entity with fields:
  - `id: Long` (primary key, auto-generated)
  - `pocId: Long` (foreign key, non-null)
  - `phase: Phase` (enum, non-null)
  - `description: String` (max 500 chars, non-null)
  - `completed: Boolean` (default false)
  - `completedAt: LocalDateTime?` (nullable)
  - `notes: String?` (max 1000 chars, nullable)
  - `displayOrder: Int` (non-null)
- Add validation annotations
- Configure cascade delete relationship to POC
- Add KDoc comments
- Create `RequirementRepository` interface extending JpaRepository

---

### [T-7] Create POC entity
**Status**: DONE  
**Requirements**: FR-1, FR-2, FR-3, DATA-1, DATA-2, DATA-3, DATA-4, DATA-10, NFR-19  
**Description**:
- Create `Poc` JPA entity with fields:
  - `id: Long` (primary key, auto-generated)
  - `customerName: String` (max 200 chars, non-null)
  - `title: String` (max 200 chars, non-null)
  - `description: String?` (max 2000 chars, nullable)
  - `dealValue: BigDecimal` (non-null, positive)
  - `projectedCloseDate: LocalDate` (non-null)
  - `kickoffDate: LocalDate` (non-null)
  - `endDate: LocalDate` (non-null, >= kickoffDate)
  - `currentPhase: Phase` (enum, non-null, default DISCOVERY)
  - `ownerId: Long` (foreign key to User, non-null)
  - `status: Status` (enum, non-null, default ACTIVE)
  - `createdAt: LocalDateTime` (non-null, auto-set)
  - `updatedAt: LocalDateTime` (non-null, auto-updated)
- Add validation annotations including custom validator for endDate >= kickoffDate
- Add relationship to User (many-to-one)
- Add relationship to Requirements (one-to-many with cascade)
- Add KDoc comments
- Create `PocRepository` interface extending JpaRepository

---

### [T-8] Create database initialization script with seed data
**Status**: DONE  
**Requirements**: DATA-15, DATA-16  
**Description**:
- Create `data.sql` or Kotlin-based database initializer
- Add 3 seed users:
  - Alice Johnson (alice@example.com, SE)
  - Bob Smith (bob@example.com, CSE)
  - Carol Davis (carol@example.com, SALES_ENGINEER)
- Create a configuration class or service to initialize default requirements for each phase (from FR-21)
- Store default requirements as data that can be retrieved when creating POCs
- Verify seed data loads on application startup

---

### [T-9] Create exception classes
**Status**: DONE  
**Requirements**: NFR-12, NFR-13  
**Description**:
- Create custom exception classes:
  - `ResourceNotFoundException` (for 404 scenarios)
  - `ValidationException` (for 400 scenarios related to business rules)
  - `PhaseTransitionException` (for invalid phase transitions)
- Add KDoc comments
- These will be used in service layer and handled by controller advice

---

### [T-10] Create global exception handler
**Status**: DONE  
**Requirements**: NFR-12, NFR-13  
**Description**:
- Create `@RestControllerAdvice` class to handle exceptions globally
- Map exceptions to appropriate HTTP status codes:
  - ResourceNotFoundException → 404
  - ValidationException → 400
  - MethodArgumentNotValidException → 400
  - PhaseTransitionException → 400
  - General Exception → 500
- Return consistent error response format with message and error code
- Add KDoc comments

---

## Phase 1 – Core Features

**Goal**: Implement core POC management, phase transitions, and requirement tracking.

### [T-11] Create DTOs for POC operations
**Status**: DONE  
**Requirements**: API-1, API-2, API-5, API-7, NFR-14, NFR-15  
**Description**:
- Create data transfer objects (DTOs):
  - `CreatePocRequest` - for POST /api/v1/pocs
  - `UpdatePocRequest` - for PUT /api/v1/pocs/{id}
  - `PocSummaryResponse` - for GET /api/v1/pocs (list)
  - `PocDetailResponse` - for GET /api/v1/pocs/{id} (single)
  - `ClosePocRequest` - for POST /api/v1/pocs/{id}/close
- Add validation annotations to request DTOs
- Ensure date/timestamp formatting per NFR-14, NFR-15
- Add KDoc comments

---

### [T-12] Create POC service - basic CRUD operations
**Status**: DONE  
**Requirements**: FR-1, FR-2, FR-3, FR-4, FR-7, FR-8, FR-9, FR-10, DATA-2, DATA-3, DATA-4, NFR-17, NFR-18  
**Description**:
- Create `PocService` class with methods:
  - `createPoc(request: CreatePocRequest): Poc` - implements FR-1, FR-2, FR-3
  - `getAllPocs(): List<Poc>` - implements FR-4
  - `getPocById(id: Long): Poc` - implements FR-7
  - `updatePoc(id: Long, request: UpdatePocRequest): Poc` - implements FR-8, FR-9
  - `deletePoc(id: Long)` - implements FR-10
- Implement business logic:
  - On create: set phase to DISCOVERY, status to ACTIVE, generate requirements
  - On update: validate POC is not in terminal phase (FR-9)
  - Validate ownerId references valid user (DATA-2)
  - Validate endDate >= kickoffDate (DATA-3)
  - Validate dealValue > 0 (DATA-4)
- Use dependency injection for repositories
- Add KDoc comments to all public methods
- Handle exceptions appropriately (throw ResourceNotFoundException, ValidationException)

---

### [T-13] Create POC service - status calculation
**Status**: DONE  
**Requirements**: FR-28, FR-29  
**Description**:
- Add method to PocService: `calculateStatus(poc: Poc): Status`
- Implement logic:
  - If phase is terminal (CLOSED_WON, CLOSED_LOST) → CLOSED
  - If current date > endDate and phase is non-terminal → AT_RISK
  - Otherwise → ACTIVE
- Call this method when retrieving POCs (on-demand calculation)
- Update POC status in database if changed
- Add KDoc comments

---

### [T-14] Create POC service - filtering and search
**Status**: DONE  
**Requirements**: FR-5, FR-6  
**Description**:
- Add method to PocService: `filterPocs(phase: Phase?, ownerId: Long?, status: Status?, search: String?): List<Poc>`
- Implement filtering logic:
  - Filter by phase if provided
  - Filter by ownerId if provided
  - Filter by status if provided (recalculate statuses first)
  - Search by customer name or title (case-insensitive, partial match) if search provided
- Use Spring Data JPA query methods or Specifications
- Add KDoc comments

---

### [T-15] Create POC REST controller - CRUD endpoints
**Status**: DONE  
**Requirements**: API-1, API-3, API-4, API-5, API-6, API-7, API-8, API-9, API-10, API-11, API-12, NFR-18
**Description**:
- Create `PocController` with `@RestController` and `@RequestMapping("/api/v1/pocs")`
- Implement endpoints:
  - `GET /api/v1/pocs` - list/filter POCs (API-4, API-5)
  - `GET /api/v1/pocs/{id}` - get single POC (API-6)
  - `POST /api/v1/pocs` - create POC (API-7, API-8)
  - `PUT /api/v1/pocs/{id}` - update POC (API-9, API-10)
  - `DELETE /api/v1/pocs/{id}` - delete POC (API-11, API-12)
- Use @Valid for request body validation
- Map service results to appropriate DTOs
- Return correct HTTP status codes and headers (e.g., Location header on POST)
- Use dependency injection for PocService
- Add KDoc comments

---

### [T-16] Create requirement service
**Status**: DONE  
**Requirements**: FR-20, FR-21, FR-22, FR-23, FR-24, FR-25, FR-26, NFR-17, NFR-18
**Description**:
- Create `RequirementService` class with methods:
  - `getRequirementsForPoc(pocId: Long): List<Requirement>` - get all requirements for POC's current phase
  - `updateRequirement(pocId: Long, requirementId: Long, completed: Boolean?, notes: String?): Requirement` - update requirement
  - `calculateCompletionPercentage(pocId: Long): Double` - calculate % complete for current phase
  - `generateDefaultRequirements(pocId: Long, phase: Phase): List<Requirement>` - create default requirements from seed data/config
- Implement business logic:
  - On marking complete: set completedAt timestamp (FR-23)
  - On marking incomplete: clear completedAt (FR-24)
  - Validate notes length (FR-25)
- Use dependency injection for repositories
- Add KDoc comments
- Handle exceptions appropriately

---

### [T-17] Create DTOs for requirement operations
**Status**: DONE  
**Requirements**: API-18, API-19, API-20
**Description**:
- Create DTOs:
  - `RequirementResponse` - for GET /api/v1/pocs/{pocId}/requirements
  - `UpdateRequirementRequest` - for PATCH /api/v1/pocs/{pocId}/requirements/{requirementId}
- Add validation annotations
- Add KDoc comments

---

### [T-18] Create requirement REST controller
**Status**: DONE  
**Requirements**: API-17, API-18, API-19, API-20, NFR-18
**Description**:
- Create `RequirementController` with appropriate @RequestMapping
- Implement endpoints:
  - `GET /api/v1/pocs/{pocId}/requirements` - list requirements (API-17, API-18)
  - `PATCH /api/v1/pocs/{pocId}/requirements/{requirementId}` - update requirement (API-19, API-20)
- Use @Valid for request body validation
- Map service results to DTOs
- Return correct HTTP status codes
- Use dependency injection for RequirementService
- Add KDoc comments

---

### [T-19] Create phase transition service methods
**Status**: DONE  
**Requirements**: FR-11, FR-12, FR-13, FR-14, FR-15, FR-16, FR-17, FR-18, FR-19, NFR-17, NFR-18
**Description**:
- Add methods to PocService:
  - `advancePhase(pocId: Long): Poc` - advance POC to next phase (FR-12, FR-13, FR-14, FR-15)
  - `closePoc(pocId: Long, outcome: String, notes: String?): Poc` - close POC as WON or LOST (FR-17, FR-18)
- Implement business logic:
  - advancePhase:
    - Check all current phase requirements are complete (FR-12, FR-13)
    - If incomplete, throw PhaseTransitionException with list of incomplete requirements (FR-14)
    - Prevent transition from terminal phases (FR-16)
    - Advance to next phase, generate new requirements, update timestamps (FR-15)
  - closePoc:
    - Validate outcome is "WON" or "LOST"
    - Set phase to CLOSED_WON or CLOSED_LOST (FR-18)
    - Set status to CLOSED
    - Optionally store closure notes
- Add KDoc comments
- Handle exceptions appropriately

---

### [T-20] Create phase transition REST endpoints
**Status**: DONE  
**Requirements**: API-13, API-14, API-15, API-16, NFR-18
**Description**:
- Add endpoints to PocController:
  - `POST /api/v1/pocs/{id}/advance` - advance phase (API-13, API-14)
  - `POST /api/v1/pocs/{id}/close` - close POC (API-15, API-16)
- Use @Valid for request body validation
- Map service results to DTOs
- Return correct HTTP status codes (200 on success, 400 on validation error)
- Add KDoc comments

---

### [T-21] Create user service
**Status**: DONE  
**Requirements**: FR-33, FR-34, NFR-17, NFR-18
**Description**:
- Create `UserService` class with methods:
  - `getAllUsers(): List<User>` - get all users
  - `getUserById(id: Long): User` - get single user
- Use dependency injection for UserRepository
- Add KDoc comments
- Handle exceptions appropriately (throw ResourceNotFoundException)

---

### [T-22] Create DTOs for user operations
**Status**: DONE  
**Requirements**: API-24, API-26
**Description**:
- Create DTOs:
  - `UserResponse` - for user endpoints
- Add KDoc comments

---

### [T-23] Create user REST controller
**Status**: DONE  
**Requirements**: API-23, API-24, API-25, API-26, NFR-18
**Description**:
- Create `UserController` with `@RestController` and `@RequestMapping("/api/v1/users")`
- Implement endpoints:
  - `GET /api/v1/users` - list users (API-23, API-24)
  - `GET /api/v1/users/{id}` - get single user (API-25, API-26)
- Map service results to DTOs
- Return correct HTTP status codes
- Use dependency injection for UserService
- Add KDoc comments

---

### [T-24] Create dashboard service
**Status**: DONE  
**Requirements**: FR-30, FR-31, FR-32, NFR-17, NFR-18
**Description**:
- Create `DashboardService` class with methods:
  - `getSummaryMetrics(): DashboardSummary` - calculate summary metrics (FR-30)
- Implement business logic:
  - Calculate active POC count (status = ACTIVE or AT_RISK)
  - Calculate at-risk POC count (status = AT_RISK)
  - Calculate total pipeline value (sum of dealValue for active POCs)
  - Calculate POCs by phase (count for each phase)
  - Ensure statuses are recalculated before aggregating
- Use dependency injection for PocRepository
- Add KDoc comments

---

### [T-25] Create DTOs for dashboard
**Status**: DONE  
**Requirements**: API-21, API-22
**Description**:
- Create DTOs:
  - `DashboardSummaryResponse` - for GET /api/v1/dashboard/summary
  - Include fields: activePocCount, atRiskPocCount, totalPipelineValue, pocsByPhase (map)
- Add KDoc comments

---

### [T-26] Create dashboard REST controller
**Status**: DONE  
**Requirements**: API-21, API-22, NFR-18
**Description**:
- Create `DashboardController` with `@RestController` and `@RequestMapping("/api/v1/dashboard")`
- Implement endpoint:
  - `GET /api/v1/dashboard/summary` - get dashboard summary (API-21, API-22)
- Map service results to DTOs
- Return correct HTTP status codes (200)
- Use dependency injection for DashboardService
- Add KDoc comments

---

## Phase 2 – Testing & Quality

**Goal**: Ensure code quality, test coverage, and system reliability.

### [T-27] Write unit tests for POC service
**Status**: DONE  
**Requirements**: NFR-20, NFR-22
**Description**:
- Create test class `PocServiceTest`
- Write unit tests for all PocService methods:
  - Test createPoc with valid data
  - Test createPoc with invalid data (validation failures)
  - Test updatePoc for non-terminal POCs
  - Test updatePoc for terminal POCs (should fail)
  - Test status calculation logic
  - Test filtering and search functionality
  - Test advancePhase with complete requirements
  - Test advancePhase with incomplete requirements (should fail)
  - Test closePoc with valid outcomes
- Use Mockito to mock repositories
- Use H2 in-memory database for tests
- Aim for 80%+ code coverage
- Add comments explaining test scenarios

---

### [T-28] Write unit tests for requirement service
**Status**: DONE  
**Requirements**: NFR-20, NFR-22
**Description**:
- Create test class `RequirementServiceTest`
- Write unit tests for all RequirementService methods:
  - Test getRequirementsForPoc
  - Test updateRequirement (marking complete/incomplete)
  - Test updateRequirement with notes
  - Test calculateCompletionPercentage
  - Test generateDefaultRequirements for each phase
- Use Mockito to mock repositories
- Use H2 in-memory database for tests
- Aim for 80%+ code coverage

---

### [T-29] Write integration tests for POC endpoints
**Status**: DONE  
**Requirements**: NFR-21, NFR-22
**Description**:
- Create test class `PocControllerIntegrationTest` with `@SpringBootTest` and `@AutoConfigureMockMvc`
- Write integration tests for all POC endpoints:
  - GET /api/v1/pocs (list, filter, search)
  - GET /api/v1/pocs/{id} (success and 404)
  - POST /api/v1/pocs (success and validation errors)
  - PUT /api/v1/pocs/{id} (success and validation errors)
  - DELETE /api/v1/pocs/{id}
  - POST /api/v1/pocs/{id}/advance (success and validation errors)
  - POST /api/v1/pocs/{id}/close (success and validation errors)
- Use MockMvc to test endpoints
- Verify HTTP status codes, headers, and response bodies
- Use H2 in-memory database for tests

---

### [T-30] Write integration tests for requirement endpoints
**Status**: DONE  
**Requirements**: NFR-21, NFR-22
**Description**:
- Create test class `RequirementControllerIntegrationTest`
- Write integration tests for requirement endpoints:
  - GET /api/v1/pocs/{pocId}/requirements
  - PATCH /api/v1/pocs/{pocId}/requirements/{requirementId}
- Use MockMvc to test endpoints
- Verify HTTP status codes and response bodies
- Use H2 in-memory database for tests

---

### [T-31] Write integration tests for user endpoints
**Status**: DONE  
**Requirements**: NFR-21, NFR-22
**Description**:
- Create test class `UserControllerIntegrationTest`
- Write integration tests for user endpoints:
  - GET /api/v1/users
  - GET /api/v1/users/{id}
- Use MockMvc to test endpoints
- Verify HTTP status codes and response bodies
- Use H2 in-memory database for tests

---

### [T-32] Write integration tests for dashboard endpoint
**Status**: DONE  
**Requirements**: NFR-21, NFR-22
**Description**:
- Create test class `DashboardControllerIntegrationTest`
- Write integration tests for dashboard endpoint:
  - GET /api/v1/dashboard/summary
  - Verify calculated metrics are correct
- Seed test data with known POCs in various phases and statuses
- Use MockMvc to test endpoint
- Verify HTTP status codes and response body values
- Use H2 in-memory database for tests

---

### [T-33] Add logging to service layer
**Status**: DONE  
**Requirements**: NFR-16, NFR-17  
**Description**:
- Add SLF4J logger to all service classes
- Log important operations:
  - POC created/updated/deleted
  - Phase transitions
  - Requirement completions
  - Errors and exceptions
- Use appropriate log levels (INFO for operations, WARN for validation issues, ERROR for exceptions)
- Ensure no sensitive data is logged

---

## Phase 3 – Documentation & Polish

**Goal**: Finalize documentation, configuration, and deployment readiness.

### [T-34] Create API documentation with Swagger/OpenAPI
**Status**: DONE  
**Requirements**: NFR-12, NFR-13
**Description**:
- Add springdoc-openapi dependency to build.gradle.kts
- Add Swagger annotations to controllers and DTOs
- Configure Swagger UI at /swagger-ui.html
- Ensure all endpoints are documented with:
  - Request/response examples
  - HTTP status codes
  - Error responses
- Verify Swagger UI is accessible and functional

---

### [T-35] Configure CORS for frontend integration
**Status**: DONE  
**Requirements**: API-1, API-2
**Description**:
- Create CORS configuration class
- Allow frontend origins (configurable via application.properties)
- Allow appropriate HTTP methods (GET, POST, PUT, PATCH, DELETE)
- Allow appropriate headers (Content-Type, Authorization, etc.)
- Configure for development and production environments

---

### [T-36] Update README.md with project documentation
**Status**: DONE  
**Requirements**: NFR-16, NFR-23  
**Description**:
- Update README.md with:
  - Project overview and purpose
  - Technology stack
  - Prerequisites (Java 17, Gradle)
  - How to run the application locally
  - How to run tests
  - How to access H2 console
  - How to access Swagger UI
  - API endpoint summary
  - Configuration options
  - Link to spec-driven-development docs

---

### [T-37] Create Docker configuration
**Status**: DONE  
**Requirements**: NFR-24  
**Description**:
- Create Dockerfile for Spring Boot application
- Create docker-compose.yml for local development (app + PostgreSQL)
- Document Docker build and run commands in README.md
- Test Docker image builds and runs successfully

---

### [T-38] Configure PostgreSQL support
**Status**: DONE  
**Requirements**: NFR-25  
**Description**:
- Add PostgreSQL driver dependency to build.gradle.kts
- Create application-prod.properties with PostgreSQL configuration
- Document PostgreSQL setup in README.md
- Ensure application works with both H2 (dev) and PostgreSQL (prod) without code changes

---

### [T-39] Add health check endpoint
**Status**: DONE  
**Requirements**: NFR-6, NFR-8  
**Description**:
- Add spring-boot-starter-actuator dependency
- Configure /actuator/health endpoint
- Make health endpoint public (no authentication required)
- Test health endpoint returns 200 OK when application is running

---

### [T-40] Performance testing and optimization
**Status**: DONE  
**Requirements**: NFR-1, NFR-2, NFR-3, NFR-4, NFR-5  
**Description**:
- Create performance test script or use JMeter/Gatling ✓
- Test POC list endpoint with 1000 POCs (verify < 2s response time) ✓
- Test POC detail endpoint (verify < 500ms response time) ✓
- Test concurrent POC creation/update with 10 users ✓
- Add database indexes if needed to improve query performance (not needed - performance excellent)
- Document performance test results ✓

**Results**:
- POC List Endpoint (1000 POCs): 0.019s (requirement: < 2s) ✓
- POC List with Phase Filter: 0.007s ✓
- POC List with Status Filter: 0.007s ✓
- POC Detail Endpoint: 0.003s (requirement: < 500ms) ✓
- 10 Concurrent Requests: 0.026s total, 0.002s average per request ✓
- All performance requirements exceeded with significant margin
- No database indexing needed at current scale

---

## Phase 4 – Future Enhancements (Optional / Deferred)

**Goal**: Features for future iterations, not part of MVP.

### [T-41] Implement authentication with Spring Security
**Status**: DONE  
**Requirements**: NFR-8, FR-35, FR-36
**Description**:
- Add Spring Security dependency ✓
- Implement JWT-based authentication ✓
- Secure all endpoints except /actuator/health and Swagger UI ✓
- Create user authentication mechanism (login/register) ✓
- Implement role-based access control ✓
- Added password field to User entity ✓
- Created SecurityConfig with JWT filter chain ✓
- Created JwtUtil for token generation and validation ✓
- Created JwtAuthenticationFilter for request authentication ✓
- Created AuthService with login and register methods ✓
- Created AuthController with /api/auth/login and /api/auth/register endpoints ✓
- Created AuthDtos (LoginRequest, LoginResponse, RegisterRequest) ✓
- All 14 authentication integration tests passing ✓
- **Note**: JWT-based authentication as specified in Open Question #2

---

### [T-42] Add audit trail / phase transition history
**Status**: DONE  
**Requirements**: FR-15, DATA-8  
**Description**:
- Create PhaseTransition entity (as described in spec.md section 7) ✓
- Store phase transitions with timestamps and user references ✓
- Add endpoint to retrieve phase transition history for a POC ✓
- **Note**: Optional for v1, marked as "optional for audit trail" in spec.md

---

### [T-43] Support custom requirements per POC
**Status**: TODO (Deferred)  
**Requirements**: FR-27  
**Description**:
- Add endpoint to create custom requirements for a POC
- Allow users to add/edit/delete requirements beyond defaults
- Update phase advancement logic to consider custom requirements
- **Note**: Deferred based on Open Question #3 in spec.md

---

### [T-44] Add export functionality (CSV/PDF)
**Status**: TODO (Deferred)  
**Requirements**: None (Future feature)  
**Description**:
- Add endpoint to export POC list as CSV
- Add endpoint to export POC details as PDF
- **Note**: Deferred based on Open Question #9 in spec.md

---

### [T-45] Implement frontend SPA
**Status**: DONE  
**Requirements**: All UX/Screen requirements from spec.md section 5  
**Description**:
- Created React SPA with TypeScript and Vite ✓
- Implemented all screens described in spec.md section 5 ✓
  - Screen 1: Dashboard with summary cards and POC list table ✓
  - Screen 2: POC Detail View with business metrics, phase timeline, requirements checklist ✓
  - Screen 3: Create/Edit POC Form with validation ✓
  - Screen 4: POC List/Search with filters (phase, status, owner, search) ✓
  - Login and Register pages ✓
- Integrated with backend REST APIs using Axios ✓
- Created comprehensive type definitions for all domain entities ✓
- Implemented authentication context with JWT token management ✓
- Built reusable UI components (Button, Input, Card, Badge, Layout) ✓
- Configured Tailwind CSS with custom color palette (primary, success, warning, danger) ✓
- Set up React Router with protected and public routes ✓
- Frontend builds successfully and is ready for deployment ✓
- **Note**: Frontend can be deployed separately from backend as a static SPA

---

## Working with an AI Agent

### Instructions for AI Coding Agents

When working on this project, AI agents MUST follow these rules:

1. **Read Documentation First**:
   - ALWAYS read `docs/spec-driven-development/spec.md` to understand the product vision and design
   - ALWAYS read `docs/spec-driven-development/requirements.md` to understand what needs to be implemented
   - ALWAYS read `docs/spec-driven-development/tasks.md` (this file) to see the implementation plan

2. **Task Selection**:
   - Do NOT write or modify application code until explicitly instructed to implement specific tasks
   - When instructed to "start implementing" or "work on tasks", begin with tasks in order: [T-0], [T-1], [T-2], etc.
   - Complete all tasks in Phase 0 before moving to Phase 1
   - If a task is unclear or blocked, ask for clarification before proceeding

3. **Task Execution**:
   - Work on one task at a time
   - Update this tasks.md file to mark tasks as IN PROGRESS when started
   - Reference the requirement IDs listed in each task
   - Update this tasks.md file to mark tasks as DONE when completed
   - If you discover issues with requirements or need new requirements, update spec.md and requirements.md FIRST, then update tasks.md with new or modified tasks

4. **Code Quality**:
   - Follow all NFR requirements (coding conventions, comments, layering, etc.)
   - Add KDoc comments to all public classes and methods
   - Write tests as specified in Phase 2 tasks
   - Ensure all code passes validation before marking a task as DONE

5. **Verification**:
   - After completing each task, verify the code compiles and runs
   - Run relevant tests to ensure no regressions
   - If using MCP tools (Playwright, Chrome DevTools) for verification, only do so when a task explicitly requires it

6. **Documentation Updates**:
   - If you discover that the spec or requirements are incomplete or incorrect, update those documents first
   - Keep this tasks.md file up-to-date with progress
   - Add new tasks if needed, following the same format and conventions

7. **Asking for Help**:
   - If blocked on an Open Question from spec.md, ask the human for a decision
   - If a task is too large or ambiguous, ask for clarification or propose breaking it into smaller tasks
   - If tests fail unexpectedly, report the issue and ask for guidance

---

**Document Status**: Draft v1.0 - Ready for Review  
**Last Updated**: 2025-11-19  
**Total Tasks**: 45 (40 core tasks + 5 deferred/optional)
