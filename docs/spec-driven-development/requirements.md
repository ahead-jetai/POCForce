# Requirements – POCForce

## 1. Functional Requirements

### POC Management

**[FR-1]** The system SHALL allow users to create a new POC with the following required fields:
- Customer name (non-empty string, max 200 characters)
- POC title (non-empty string, max 200 characters)
- Deal value (positive decimal number)
- Projected close date (valid future or present date)
- POC kickoff date (valid date)
- POC end date (valid date, must be >= kickoff date)
- Assigned owner/SE (reference to valid User)

**[FR-2]** The system SHALL allow users to create a new POC with the following optional fields:
- POC description (text, max 2000 characters)

**[FR-3]** When a new POC is created, the system SHALL automatically:
- Set the current phase to DISCOVERY
- Set the status to ACTIVE
- Generate a set of default requirements for the DISCOVERY phase
- Assign a unique identifier to the POC
- Record creation timestamp

**[FR-4]** The system SHALL allow users to view a list of all POCs with the following information displayed:
- Customer name
- POC title
- Current phase
- Owner name
- Deal value
- Kickoff date
- End date
- Status (ACTIVE, AT_RISK, CLOSED)

**[FR-5]** The system SHALL allow users to filter the POC list by:
- Current phase (single or multiple selection)
- Owner (single selection)
- Status (ACTIVE, AT_RISK, CLOSED)

**[FR-6]** The system SHALL allow users to search POCs by:
- Customer name (partial match, case-insensitive)
- POC title (partial match, case-insensitive)

**[FR-7]** The system SHALL allow users to view detailed information for a single POC, including:
- All POC attributes (customer name, title, description, dates, deal value, owner)
- Current phase
- Current status
- List of requirements for the current phase with completion status
- Completion percentage for current phase requirements

**[FR-8]** The system SHALL allow users to edit POC details (customer name, title, description, dates, deal value, owner) for POCs not in terminal phases (CLOSED_WON, CLOSED_LOST).

**[FR-9]** The system SHALL prevent editing of POCs in terminal phases (CLOSED_WON, CLOSED_LOST).

**[FR-10]** The system SHALL allow users to delete POCs. (Implementation detail: soft vs. hard delete to be determined based on Open Question #5 in spec.md)

### Phase Management

**[FR-11]** The system SHALL support the following phase sequence:
1. DISCOVERY (initial phase)
2. PLANNING
3. EXECUTION
4. VALIDATION
5. CLOSED_WON (terminal)
6. CLOSED_LOST (terminal)

**[FR-12]** The system SHALL allow users to advance a POC to the next phase in the sequence (e.g., DISCOVERY → PLANNING, PLANNING → EXECUTION, etc.) if and only if all requirements for the current phase are marked as complete.

**[FR-13]** The system SHALL prevent phase advancement if any requirement for the current phase is incomplete.

**[FR-14]** When a user attempts to advance a POC with incomplete requirements, the system SHALL display an error message listing the incomplete requirements.

**[FR-15]** When a POC successfully advances to a new phase, the system SHALL:
- Update the current phase to the next phase
- Generate default requirements for the new phase
- Record the phase transition with a timestamp
- Update the POC's updated timestamp

**[FR-16]** The system SHALL prevent phase transitions from terminal phases (CLOSED_WON, CLOSED_LOST) to any other phase.

**[FR-17]** The system SHALL allow users to close a POC by selecting either CLOSED_WON or CLOSED_LOST as the terminal phase.

**[FR-18]** When closing a POC, the system SHALL:
- Update the current phase to the selected terminal phase (CLOSED_WON or CLOSED_LOST)
- Update the status to CLOSED
- Record the phase transition with a timestamp
- Optionally accept closure notes (text field)

**[FR-19]** Phase transitions SHALL be strictly forward-only; the system SHALL NOT allow regression to previous phases. (Note: This is based on Assumption #2; see Open Question #7 in spec.md)

### Requirement Management

**[FR-20]** Each phase SHALL have a predefined set of default requirements that are automatically created when a POC enters that phase.

**[FR-21]** Default requirements for each phase SHALL include:
- **DISCOVERY**:
  - Identify key stakeholders and decision-makers
  - Define success criteria and evaluation metrics
  - Understand customer's technical environment
  - Document customer pain points and use cases
  - Establish communication plan and schedule
- **PLANNING**:
  - Finalize POC scope and objectives
  - Set up POC environment (dev/staging)
  - Confirm data requirements and access
  - Create detailed POC timeline with milestones
  - Identify potential risks and mitigation strategies
- **EXECUTION**:
  - Configure product for customer use case
  - Conduct initial demo/training session
  - Provide customer access to POC environment
  - Monitor usage and gather feedback
  - Address technical issues and questions
- **VALIDATION**:
  - Review POC results against success criteria
  - Conduct final demo/presentation
  - Obtain customer feedback and satisfaction rating
  - Document lessons learned
  - Customer sign-off on POC outcomes

**[FR-22]** The system SHALL allow users to mark a requirement as complete by checking a checkbox or similar UI control.

**[FR-23]** When a requirement is marked as complete, the system SHALL record the completion timestamp.

**[FR-24]** The system SHALL allow users to mark a previously completed requirement as incomplete (unchecking).

**[FR-25]** The system SHALL allow users to add optional notes to individual requirements (text field, max 1000 characters).

**[FR-26]** The system SHALL calculate and display the completion percentage for the current phase based on the ratio of completed requirements to total requirements.

**[FR-27]** The system MAY allow users to add custom requirements to a POC after creation. (Note: To be determined based on Open Question #3 in spec.md; if not supported in v1, this requirement is deferred)

### Status Management

**[FR-28]** The system SHALL automatically calculate and update the status of each POC based on the following rules:
- **ACTIVE**: POC is in a non-terminal phase (DISCOVERY, PLANNING, EXECUTION, VALIDATION) and current date <= end date
- **AT_RISK**: POC is in a non-terminal phase and current date > end date
- **CLOSED**: POC is in a terminal phase (CLOSED_WON, CLOSED_LOST)

**[FR-29]** Status updates SHALL occur automatically when:
- A POC is viewed or listed (on-demand calculation)
- A scheduled background job runs (e.g., daily status refresh)

### Dashboard & Reporting

**[FR-30]** The system SHALL provide a dashboard view displaying the following summary metrics:
- Total count of active POCs (status = ACTIVE or AT_RISK)
- Count of at-risk POCs (status = AT_RISK)
- Total pipeline value (sum of deal values for all POCs with status = ACTIVE or AT_RISK)
- Count of POCs by phase (breakdown: DISCOVERY, PLANNING, EXECUTION, VALIDATION, CLOSED_WON, CLOSED_LOST)

**[FR-31]** The dashboard SHALL display a list of POCs sorted by default in descending order of creation date (most recent first).

**[FR-32]** Users SHALL be able to sort the POC list by:
- Customer name (ascending/descending)
- Deal value (ascending/descending)
- End date (ascending/descending)
- Current phase
- Status

### User Management (Simplified)

**[FR-33]** The system SHALL maintain a list of users with the following attributes:
- Unique identifier
- Name
- Email (unique)
- Role (SE, CSE, SALES_ENGINEER, MANAGER)

**[FR-34]** The system SHALL allow selection of any user as a POC owner when creating or editing a POC.

**[FR-35]** All authenticated users SHALL have full read and write access to all POCs. (Note: Based on Assumption #4; no RBAC in v1)

**[FR-36]** Users with role MANAGER MAY have read-only access enforced at the application level. (Note: Backend enforcement not required in v1; see Assumption #4)

## 2. Non-Functional Requirements

### Performance

**[NFR-1]** The system SHALL respond to POC list requests (unfiltered) within 2 seconds for up to 1000 POCs.

**[NFR-2]** The system SHALL respond to single POC detail requests within 500 milliseconds.

**[NFR-3]** The system SHALL handle concurrent creation or update of POCs by up to 10 simultaneous users without data corruption or significant performance degradation.

### Scalability

**[NFR-4]** The system SHALL support storage of at least 10,000 POCs without performance degradation.

**[NFR-5]** The system SHALL support storage of at least 100,000 requirements (across all POCs) without performance degradation.

### Reliability

**[NFR-6]** The system SHALL have an uptime of at least 99% during business hours (8 AM - 6 PM local time, Monday-Friday).

**[NFR-7]** Phase transitions and requirement updates SHALL be atomic; partial updates SHALL NOT occur even in case of system failure.

### Security

**[NFR-8]** The system SHALL require user authentication for all API endpoints except health check endpoints.

**[NFR-9]** The system SHALL use HTTPS for all client-server communication in production deployments.

**[NFR-10]** The system SHALL validate all user inputs to prevent injection attacks (SQL injection, XSS, etc.).

**[NFR-11]** The system SHALL sanitize user-provided text fields (descriptions, notes) before storing or displaying.

### Usability

**[NFR-12]** API responses SHALL use standard HTTP status codes (200, 201, 400, 404, 500, etc.) consistently.

**[NFR-13]** API error responses SHALL include a human-readable error message and a machine-readable error code.

**[NFR-14]** All date fields SHALL use ISO 8601 format (YYYY-MM-DD) in API requests and responses.

**[NFR-15]** All timestamp fields SHALL use ISO 8601 format with timezone (e.g., 2025-11-19T17:52:00Z) in API responses.

### Maintainability

**[NFR-16]** The codebase SHALL follow Kotlin coding conventions and Spring Boot best practices.

**[NFR-17]** All public methods in the service layer SHALL have KDoc comments describing their purpose, parameters, and return values.

**[NFR-18]** The system SHALL use dependency injection for all cross-layer dependencies (controllers → services → repositories).

**[NFR-19]** The system SHALL separate concerns into distinct layers: controller, service, repository, and domain model.

### Testability

**[NFR-20]** The system SHALL include unit tests for all service layer business logic with at least 80% code coverage.

**[NFR-21]** The system SHALL include integration tests for all REST API endpoints.

**[NFR-22]** The system SHALL support running tests with an in-memory database (H2) without requiring external database setup.

### Portability

**[NFR-23]** The system SHALL run on any platform supporting Java 17 or higher (Linux, macOS, Windows).

**[NFR-24]** The system SHALL support deployment as a standalone JAR file or Docker container.

**[NFR-25]** The system SHALL support both H2 (in-memory) and PostgreSQL databases without code changes (configuration-only).

## 3. API Requirements

### General API Design

**[API-1]** All API endpoints SHALL use the `/api/v1` base path prefix.

**[API-2]** All API requests and responses SHALL use JSON format with `Content-Type: application/json`.

**[API-3]** All API endpoints SHALL follow RESTful naming conventions:
- Collections: `/api/v1/pocs`
- Single resource: `/api/v1/pocs/{id}`
- Sub-resources: `/api/v1/pocs/{id}/requirements`

### POC Endpoints

**[API-4]** `GET /api/v1/pocs` SHALL return a list of all POCs with optional query parameters for filtering:
- `phase` (e.g., `?phase=DISCOVERY`)
- `ownerId` (e.g., `?ownerId=123`)
- `status` (e.g., `?status=AT_RISK`)
- `search` (e.g., `?search=Acme` searches customer name and title)

**[API-5]** `GET /api/v1/pocs` response SHALL include:
- HTTP 200 status
- JSON array of POC summary objects with fields: id, customerName, title, currentPhase, ownerId, ownerName, dealValue, kickoffDate, endDate, status

**[API-6]** `GET /api/v1/pocs/{id}` SHALL return detailed information for a single POC including:
- All POC attributes
- Embedded list of requirements for the current phase
- HTTP 200 status on success
- HTTP 404 status if POC not found

**[API-7]** `POST /api/v1/pocs` SHALL create a new POC with request body containing:
- customerName (required)
- title (required)
- description (optional)
- dealValue (required)
- projectedCloseDate (required)
- kickoffDate (required)
- endDate (required)
- ownerId (required)

**[API-8]** `POST /api/v1/pocs` response SHALL include:
- HTTP 201 status on success
- `Location` header with URL of created POC
- JSON body with created POC details including generated id
- HTTP 400 status with validation errors if input is invalid

**[API-9]** `PUT /api/v1/pocs/{id}` SHALL update an existing POC with request body containing updatable fields (same as POST, excluding phase which is managed separately).

**[API-10]** `PUT /api/v1/pocs/{id}` response SHALL include:
- HTTP 200 status on success with updated POC details
- HTTP 404 status if POC not found
- HTTP 400 status if validation fails or POC is in terminal phase

**[API-11]** `DELETE /api/v1/pocs/{id}` SHALL delete a POC.

**[API-12]** `DELETE /api/v1/pocs/{id}` response SHALL include:
- HTTP 204 status on success (no content)
- HTTP 404 status if POC not found

### Phase Transition Endpoints

**[API-13]** `POST /api/v1/pocs/{id}/advance` SHALL advance a POC to the next phase in the sequence.

**[API-14]** `POST /api/v1/pocs/{id}/advance` response SHALL include:
- HTTP 200 status on success with updated POC details
- HTTP 400 status if phase advancement is not allowed (incomplete requirements or already in terminal phase)
- HTTP 404 status if POC not found

**[API-15]** `POST /api/v1/pocs/{id}/close` SHALL close a POC with request body containing:
- outcome (required: "WON" or "LOST")
- notes (optional)

**[API-16]** `POST /api/v1/pocs/{id}/close` response SHALL include:
- HTTP 200 status on success with updated POC details (phase set to CLOSED_WON or CLOSED_LOST)
- HTTP 400 status if outcome is invalid
- HTTP 404 status if POC not found

### Requirement Endpoints

**[API-17]** `GET /api/v1/pocs/{pocId}/requirements` SHALL return all requirements for the current phase of the specified POC.

**[API-18]** `GET /api/v1/pocs/{pocId}/requirements` response SHALL include:
- HTTP 200 status
- JSON array of requirement objects with fields: id, description, completed, completedAt, notes, displayOrder
- HTTP 404 status if POC not found

**[API-19]** `PATCH /api/v1/pocs/{pocId}/requirements/{requirementId}` SHALL update a single requirement with request body containing:
- completed (optional boolean)
- notes (optional string)

**[API-20]** `PATCH /api/v1/pocs/{pocId}/requirements/{requirementId}` response SHALL include:
- HTTP 200 status on success with updated requirement details
- HTTP 404 status if POC or requirement not found
- HTTP 400 status if validation fails

### Dashboard Endpoints

**[API-21]** `GET /api/v1/dashboard/summary` SHALL return dashboard summary metrics including:
- activePocCount (integer)
- atRiskPocCount (integer)
- totalPipelineValue (decimal)
- pocsByPhase (object with phase names as keys and counts as values)

**[API-22]** `GET /api/v1/dashboard/summary` response SHALL include:
- HTTP 200 status
- JSON object with summary metrics

### User Endpoints

**[API-23]** `GET /api/v1/users` SHALL return a list of all users.

**[API-24]** `GET /api/v1/users` response SHALL include:
- HTTP 200 status
- JSON array of user objects with fields: id, name, email, role

**[API-25]** `GET /api/v1/users/{id}` SHALL return details for a single user.

**[API-26]** `GET /api/v1/users/{id}` response SHALL include:
- HTTP 200 status on success with user details
- HTTP 404 status if user not found

## 4. Data Requirements

### Data Integrity

**[DATA-1]** The POC `id` field SHALL be unique and immutable.

**[DATA-2]** The POC `ownerId` field SHALL reference a valid user; the system SHALL reject POC creation/update with an invalid ownerId.

**[DATA-3]** The POC `endDate` field SHALL be greater than or equal to `kickoffDate`; the system SHALL reject POC creation/update violating this constraint.

**[DATA-4]** The POC `dealValue` field SHALL be a positive number; the system SHALL reject POC creation/update with zero or negative values.

**[DATA-5]** The Requirement `pocId` field SHALL reference a valid POC; orphaned requirements SHALL NOT exist.

**[DATA-6]** When a POC is deleted, all associated requirements SHALL be deleted (cascade delete).

### Data Persistence

**[DATA-7]** All POC data SHALL be persisted to the database immediately upon creation or update.

**[DATA-8]** Phase transitions SHALL be recorded with timestamps for audit purposes.

**[DATA-9]** Requirement completion status changes SHALL update the `completedAt` timestamp when toggling from incomplete to complete.

**[DATA-10]** The system SHALL store timestamps in UTC timezone internally.

### Data Validation

**[DATA-11]** The system SHALL enforce maximum string lengths as specified in FR-1 and FR-2.

**[DATA-12]** The system SHALL reject POC creation/update requests with missing required fields.

**[DATA-13]** The system SHALL validate that date fields contain valid date values in ISO 8601 format.

**[DATA-14]** The system SHALL validate that enum fields (phase, status, role) contain valid enum values.

### Default Data

**[DATA-15]** The system SHALL initialize with at least 3 seed users for testing purposes:
- User 1: Alice Johnson (SE)
- User 2: Bob Smith (CSE)
- User 3: Carol Davis (SALES_ENGINEER)

**[DATA-16]** Default requirements for each phase (as specified in FR-21) SHALL be stored as configuration data or seed data, not hardcoded in business logic.

## 5. Traceability

### Spec to Requirements Mapping

| Spec Section | Requirement IDs |
|--------------|-----------------|
| **2. Goals** | All FR and NFR requirements support the goals listed |
| **4. Personas & Key Flows - Flow 1** | FR-1, FR-2, FR-3, API-7, API-8 |
| **4. Personas & Key Flows - Flow 2** | FR-7, FR-20, FR-21, FR-22, FR-23, FR-24, FR-25, FR-26, API-19, API-20 |
| **4. Personas & Key Flows - Flow 3** | FR-11, FR-12, FR-13, FR-14, FR-15, FR-16, API-13, API-14 |
| **4. Personas & Key Flows - Flow 4** | FR-17, FR-18, API-15, API-16 |
| **4. Personas & Key Flows - Flow 5** | FR-4, FR-30, FR-31, FR-32, API-4, API-5, API-21, API-22 |
| **5. UX / Screens - Screen 1** | FR-4, FR-5, FR-6, FR-30, FR-31, FR-32, API-4, API-21 |
| **5. UX / Screens - Screen 2** | FR-7, FR-8, FR-12, FR-17, FR-22, FR-26, API-6, API-13, API-15, API-19 |
| **5. UX / Screens - Screen 3** | FR-1, FR-2, FR-8, API-7, API-9 |
| **5. UX / Screens - Screen 4** | FR-4, FR-5, FR-6, API-4 |
| **6. High-Level Architecture** | NFR-16, NFR-17, NFR-18, NFR-19, NFR-22, NFR-23, NFR-24, NFR-25, API-1, API-2, API-3 |
| **7. High-Level Data Model** | DATA-1 through DATA-16, FR-11, FR-20, FR-21, FR-28, FR-33 |
| **9. Assumptions - #1 (Single Tenant)** | NFR-4, NFR-5 |
| **9. Assumptions - #2 (Fixed Phases)** | FR-11, FR-19 |
| **9. Assumptions - #3 (Default Requirements)** | FR-20, FR-21, DATA-16 |
| **9. Assumptions - #4 (Simplified User Management)** | FR-33, FR-34, FR-35, FR-36 |
| **9. Assumptions - #6 (At-Risk Logic)** | FR-28 |
| **9. Assumptions - #7 (Currency)** | FR-1 (dealValue field) |
| **9. Assumptions - #9 (Default Requirement Content)** | FR-21 |
| **9. Assumptions - #10 (No Versioning)** | FR-20 (static requirements after POC creation) |

### Requirements to Tasks Mapping

This section will be populated once tasks.md is created. Each task will reference the requirement IDs it implements.

---

**Document Status**: Draft v1.0 - Ready for Review  
**Last Updated**: 2025-11-19  
**Next Steps**: Review open questions in spec.md, finalize decisions, and create tasks.md based on these requirements.
