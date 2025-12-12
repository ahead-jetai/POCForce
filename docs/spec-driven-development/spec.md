# Spec – POCForce

## 1. Summary

POCForce is a minimalist CRM system designed specifically for tracking and managing technical Proof of Concept (POC) engagements in B2B SaaS and AI companies. Unlike traditional CRMs that attempt to cover the entire sales lifecycle, POCForce focuses exclusively on the POC phase—the critical technical validation period where Solutions Engineers, Customer Success Engineers, and Sales Engineers collaborate with prospects to demonstrate product value and fit.

The system provides structured phase management, requirement tracking, and business metrics visibility to ensure POC engagements progress systematically toward successful outcomes.

## 2. Goals

- **Specialized POC Management**: Provide a purpose-built system for tracking technical POC engagements through defined lifecycle phases
- **Phase-Gate Control**: Enforce completion of phase requirements before allowing POCs to advance to subsequent phases
- **Business Visibility**: Track key business metrics (deal value, dates, status) alongside technical progress
- **Checklist-Driven Workflow**: Guide technical sales teams through standardized best practices via phase-specific requirement checklists
- **At-Risk Detection**: Identify POCs that are falling behind schedule or stalled in specific phases
- **Audit Trail**: Maintain history of phase transitions and requirement completions for post-mortem analysis

## 3. Non-Goals

- **Full CRM Functionality**: POCForce is NOT intended to replace Salesforce, HubSpot, or other general-purpose CRMs. It does not handle lead generation, opportunity management outside of POC context, or complex pipeline forecasting
- **Marketing Automation**: No email campaigns, marketing workflows, or lead nurturing capabilities
- **Multi-Product Catalog**: The system assumes a single product/platform per POC, not complex multi-product deals
- **Financial/Billing Integration**: No invoicing, payment processing, or revenue recognition features
- **Customer Support Ticketing**: Post-sale support tracking is out of scope
- **Advanced Analytics/BI**: Basic reporting only; complex dashboards and predictive analytics are not included in v1

## 4. Personas & Key Flows

### Personas

**Primary Users:**

1. **Solutions Engineer (SE)**
   - Technical sales role
   - Owns POC execution and technical validation
   - Needs: Quick POC status updates, requirement tracking, ability to advance POCs through phases

2. **Customer Success Engineer (CSE)**
   - Post-sales technical role, sometimes involved in pre-sale POCs
   - Needs: Visibility into POC progress, handoff preparation for post-POC onboarding

3. **Sales Engineer**
   - Hybrid technical-sales role
   - Needs: Business metrics visibility (deal value, close dates), POC health indicators

**Secondary Users:**

4. **Sales Manager / Director**
   - Read-only access to monitor POC pipeline health
   - Needs: High-level dashboard of POC statuses, at-risk POCs, forecasted close dates

### Key Flows

**Flow 1: Create New POC**
1. User logs in and navigates to "Create POC"
2. Enters required information:
   - Customer/company name
   - POC title/description
   - Deal value ($)
   - Projected close date
   - POC kickoff date
   - POC end date
   - Assigned SE/owner
3. System creates POC in DISCOVERY phase with default requirements checklist
4. User sees newly created POC in their POC list

**Flow 2: Work on POC & Complete Requirements**
1. User opens an active POC
2. Views current phase and associated requirements checklist
3. Marks requirements as complete (checkbox)
4. Adds notes/comments to individual requirements (optional)
5. System tracks completion percentage for current phase

**Flow 3: Advance POC to Next Phase**
1. User attempts to advance POC to next phase (e.g., DISCOVERY → PLANNING)
2. System validates all current phase requirements are complete
3. If incomplete: System prevents advancement and displays remaining requirements
4. If complete: System advances POC to next phase, loads new phase requirements
5. System records phase transition with timestamp

**Flow 4: Close POC (Won or Lost)**
1. User navigates to POC in late phase (e.g., VALIDATION)
2. Selects "Close POC" action
3. Chooses outcome: CLOSED_WON or CLOSED_LOST
4. Optionally adds closure notes/reason
5. System marks POC as terminal (no further phase transitions allowed)
6. POC moves to historical view

**Flow 5: View POC Dashboard**
1. User lands on dashboard/home page
2. Sees summary cards:
   - Total active POCs
   - POCs by phase (counts)
   - At-risk POCs (past end date, not closed)
   - Total pipeline value ($)
3. Views list of their assigned POCs with key columns (customer, phase, dates, deal value)
4. Can filter/sort POC list

## 5. UX / Screens Overview (high-level, no pixel-perfect design)

### Screen 1: Dashboard / Home
- **Header**: App branding "POCForce", user profile, logout
- **Summary Cards**: Active POC count, at-risk count, total pipeline value, POCs by phase distribution
- **POC List Table**: Sortable/filterable table with columns:
  - Customer Name
  - POC Title
  - Current Phase
  - Owner (SE)
  - Deal Value
  - Kickoff Date
  - End Date
  - Status Indicator (on track / at risk / closed)
- **Actions**: "Create New POC" button prominent at top

### Screen 2: POC Detail View
- **Header**: Customer name, POC title, current phase badge
- **Business Metrics Panel**:
  - Deal Value
  - Projected Close Date
  - POC Kickoff Date
  - POC End Date
  - Owner/Assigned SE
- **Phase Timeline**: Visual representation of phase progression (past → current → future)
- **Requirements Checklist**:
  - List of requirements for current phase
  - Checkboxes to mark complete
  - Optional notes field per requirement
  - Completion percentage indicator
- **Actions**:
  - "Advance to Next Phase" button (enabled/disabled based on requirement completion)
  - "Close POC" button (with Won/Lost choice)
  - "Edit POC Details" button
- **Activity Log** (optional for v1): Chronological list of phase transitions and major updates

### Screen 3: Create/Edit POC Form
- Form fields:
  - Customer Name (text)
  - POC Title (text)
  - POC Description (textarea)
  - Deal Value (currency input)
  - Projected Close Date (date picker)
  - POC Kickoff Date (date picker)
  - POC End Date (date picker)
  - Assigned Owner (dropdown of users)
- **Actions**: "Save" and "Cancel" buttons

### Screen 4: POC List / Search
- Filters:
  - Phase (dropdown/multi-select)
  - Owner (dropdown)
  - Status (Active / At Risk / Closed)
  - Date ranges
- Search bar (by customer name or POC title)
- Results table (same columns as dashboard)

## 6. High-Level Architecture

### Backend
- **Framework**: Spring Boot 3.x (Kotlin)
- **Web Layer**: Spring Web (REST APIs)
- **Persistence**: Spring Data JPA with JPA/Hibernate
- **Database**: H2 in-memory for development; PostgreSQL recommended for production
- **Validation**: Bean Validation (JSR-380) for input validation
- **API Style**: RESTful JSON APIs

### Frontend (Planned, Not Implemented Yet)
- **Framework**: Modern JavaScript SPA (React, Vue, or Angular)
- **Communication**: Fetch/Axios to consume backend REST APIs
- **State Management**: Component-based state or lightweight global state (Context API, Vuex, etc.)

### Architecture Layers
1. **Controller Layer**: REST endpoints for POC CRUD, phase transitions, requirement management
2. **Service Layer**: Business logic for phase validation, at-risk detection, POC lifecycle rules
3. **Repository Layer**: Data access via Spring Data JPA repositories
4. **Domain Model**: POC, Phase, Requirement, User (simplified)

### Deployment
- **Development**: Embedded Tomcat, H2 in-memory database
- **Production**: Containerized (Docker), deployed to cloud platform (AWS, GCP, Azure), external PostgreSQL database

## 7. High-Level Data Model

### Core Entities

**POC**
- `id` (UUID or Long, primary key)
- `customerName` (String, required)
- `title` (String, required)
- `description` (Text, optional)
- `dealValue` (BigDecimal, required, represents currency)
- `projectedCloseDate` (LocalDate, required)
- `kickoffDate` (LocalDate, required)
- `endDate` (LocalDate, required)
- `currentPhase` (Enum: DISCOVERY, PLANNING, EXECUTION, VALIDATION, CLOSED_WON, CLOSED_LOST)
- `ownerId` (reference to User, required)
- `status` (Enum: ACTIVE, AT_RISK, CLOSED)
- `createdAt` (Timestamp)
- `updatedAt` (Timestamp)
- Relationship: One POC → Many Requirements
- Relationship: One POC → Many PhaseTransitions (optional, for audit trail)

**Requirement**
- `id` (UUID or Long, primary key)
- `pocId` (foreign key to POC)
- `phase` (Enum: DISCOVERY, PLANNING, EXECUTION, VALIDATION)
- `description` (String, required)
- `completed` (Boolean, default false)
- `completedAt` (Timestamp, nullable)
- `notes` (Text, optional)
- `displayOrder` (Integer, for sorting)

**User** (Simplified)
- `id` (UUID or Long, primary key)
- `name` (String)
- `email` (String, unique)
- `role` (Enum: SE, CSE, SALES_ENGINEER, MANAGER)
- Relationship: One User → Many POCs (as owner)

**PhaseTransition** (Optional for v1, audit trail)
- `id` (UUID or Long, primary key)
- `pocId` (foreign key to POC)
- `fromPhase` (Enum or null for initial)
- `toPhase` (Enum)
- `transitionedAt` (Timestamp)
- `transitionedBy` (User reference)

### Phase Enum Values
- `DISCOVERY`: Initial phase, understanding customer needs and success criteria
- `PLANNING`: POC scope definition, environment setup, timeline confirmation
- `EXECUTION`: Active POC implementation and testing
- `VALIDATION`: Customer-side validation, success criteria verification
- `CLOSED_WON`: POC successfully completed, deal moving forward
- `CLOSED_LOST`: POC ended without winning the deal

### Status Enum Values
- `ACTIVE`: POC in progress
- `AT_RISK`: POC past end date and not yet closed
- `CLOSED`: POC in terminal phase (CLOSED_WON or CLOSED_LOST)

## 8. Integration Points

**Current (v1):**
- None. POCForce operates as a standalone system.

**Future/Potential:**
- **Salesforce / CRM Sync**: Bi-directional sync of opportunity/deal data
  - Import opportunity details (customer name, deal value, close date) to create POCs
  - Update Salesforce opportunity stage when POC reaches terminal phase
- **Slack Notifications**: Send alerts to team channels when:
  - POC becomes at-risk
  - POC advances to new phase
  - POC closes (won/lost)
- **Calendar Integration**: Sync POC kickoff/end dates with Google Calendar or Outlook
- **JIRA / Project Management**: Link POC phases to JIRA epics or project tasks
- **Email Notifications**: Send digest emails to SE owners with POC status updates

## 9. Assumptions

1. **Single Tenant**: POCForce v1 assumes a single organization/company deployment. Multi-tenancy is not required initially.

2. **Phase Model is Fixed**: All POCs follow the same phase sequence (DISCOVERY → PLANNING → EXECUTION → VALIDATION → terminal). Custom phase definitions per POC are not supported.

3. **Default Requirements**: Each phase comes with a predefined set of default requirements. These requirements are the same across all POCs. Custom requirements can be added, but defaults should cover 80% of use cases.

4. **Simplified User Management**: No complex role-based access control (RBAC) in v1. All authenticated users can create/edit/view all POCs. Managers have read-only access assumed at application level, not enforced by backend.

5. **No External Auth Provider**: Initial authentication is simple (e.g., hardcoded users or basic in-memory auth). OAuth2/SAML integration is future scope.

6. **At-Risk Logic**: A POC is considered "at-risk" if its current date > end date AND it is not in a terminal phase (CLOSED_WON or CLOSED_LOST). More sophisticated at-risk logic (e.g., based on phase duration) is future enhancement.

7. **Currency**: Deal value is stored as a numeric value. Currency type (USD, EUR, etc.) is assumed to be USD or is configured globally, not per-POC.

8. **Frontend Hosting**: The frontend SPA will be hosted separately from the backend API (e.g., on a CDN or static hosting). Backend serves only JSON APIs, not HTML views.

9. **Default Requirement Content**: The system will include sensible default requirements per phase based on industry best practices for technical POCs (e.g., "Define success criteria" in DISCOVERY, "Set up POC environment" in PLANNING, "Conduct demo session" in EXECUTION, "Customer sign-off" in VALIDATION).

10. **No Versioning of Requirements**: Once a POC is created with its initial requirements, those requirements remain static. Future changes to default requirements do not retroactively affect existing POCs.

## 10. Open Questions

1. **Default Requirement Content**: What specific requirements should be included by default for each phase (DISCOVERY, PLANNING, EXECUTION, VALIDATION)? Should these be configurable by admins, or hardcoded for v1?

2. **User Authentication**: Should we use Spring Security with in-memory users for v1, or implement basic token-based auth (JWT)? What is the preferred approach for the initial version? ANSWER is JWT for v1.

3. **Editable vs. Fixed Phases**: Should users be able to add custom requirements to a POC after creation, or should the requirement list be locked once the POC is created?

4. **At-Risk Threshold**: Is the "end date passed" rule sufficient for marking POCs at-risk, or should we add additional criteria (e.g., "stuck in one phase for > N days")?

5. **POC Deletion**: Should POCs be soft-deleted (flagged as deleted but retained in DB) or hard-deleted? Should there be restrictions on deleting POCs in certain phases?

6. **Multi-Owner Support**: Should POCs support multiple owners/collaborators, or is a single owner sufficient for v1?

7. **Phase Regression**: Can a POC move backward to a previous phase (e.g., EXECUTION → PLANNING), or are phase transitions strictly forward-only?

8. **Bulk Operations**: Should the system support bulk actions (e.g., bulk update phase, bulk assign owner) from the POC list view?

9. **Export/Reporting**: What export formats are needed (CSV, PDF, Excel)? Should there be a basic reporting API or export endpoint in v1?

10. **Database Choice**: Confirm production database preference. PostgreSQL is recommended, but should we also support MySQL or other databases?
