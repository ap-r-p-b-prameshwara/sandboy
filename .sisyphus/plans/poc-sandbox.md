# PoC Sandbox Environment - Work Plan

## TL;DR

> **Quick Summary**: Build a sandbox environment proof-of-concept similar to Stripe test mode using clone services architecture with complete data isolation.
> 
> **Deliverables**: 17 Docker containers (2 DB + 1 Redis + 10 backend + 3 frontend + 1 reverse proxy), complete authentication flow, environment switching
>
> **Estimated Effort**: XL
> **Parallel Execution**: YES - 4 waves
> **Critical Path**: Infrastructure → User/Auth → Gateway → Business Services → Frontend → Testing

---

## Context

### Original Request
Build PoC for sandbox environment where merchants can test dashboard and API without affecting production data.

### Interview Summary
- **Architecture**: Clone services (minimal environment-specific logic)
- **Authentication**: Login/Registration ONLY from Production; dual-write sync to both DBs
- **Data Strategy**: User data synced, privilege differs, merchant data completely isolated
- **Tech Stack**: Spring Boot 4 + Java 25, Angular 20, PostgreSQL, Redis
- **Deployment**: Local Docker with Docker Compose
- **Testing**: Tests After Implementation

### Guardrails Applied
- NO shared database tables between environments
- NO login/registration in Sandbox UI
- NO merchant data sync between environments
- NO environment-specific logic in business services (keep pure clones)

---

## Work Objectives

### Must Have
- 17 Docker containers running successfully
- User registration creates records in both Production and Sandbox DBs
- User can login from Production UI and access Production features
- User can switch to Sandbox without re-authentication
- QRIS and Cash In features work in both environments
- Complete test coverage

### Must NOT Have
- NO cross-environment data leakage
- NO shared database tables
- NO login UI in Sandbox
- NO hardcoded credentials in code

---

## Verification Strategy

- **Automated tests**: YES (Tests After Implementation)
- **Backend**: JUnit 5 + Mockito
- **Frontend**: Jasmine/Karma
- **Agent-Executed QA**: MANDATORY for all tasks
  - UI: Playwright
  - API: curl + jq
  - DB: psql
  - Docker: docker-compose

---

## Execution Strategy

```
Wave 1 (Infrastructure & Core Services - 7 tasks parallel):
├── T1: PostgreSQL setup (prod + sandbox) [quick]
├── T2: Redis setup [quick]
├── T3: Docker Compose base [quick]
├── T4: User Service Production (dual-DB) [deep]
├── T5: User Service Sandbox (clone) [quick]
├── T6: Auth Service Production [unspecified-high]
└── T7: Auth Service Sandbox (clone) [quick]

Wave 2 (Gateway & Business Services - 6 tasks parallel):
├── T8: Gateway Service Production [unspecified-high]
├── T9: Gateway Service Sandbox [unspecified-high]
├── T10: QRIS Service Production [unspecified-high]
├── T11: QRIS Service Sandbox [quick]
├── T12: Cash In Service Production [unspecified-high]
└── T13: Cash In Service Sandbox [quick]

Wave 3 (Frontend - 3 tasks):
├── T14: Main Web (Container) [visual-engineering]
├── T15: QRIS Web (Microfrontend) [visual-engineering]
└── T16: Cash In Web (Microfrontend) [visual-engineering]

Wave 4 (Testing - 4 tasks):
├── T17: Backend unit tests [unspecified-high]
├── T18: Frontend unit tests [unspecified-high]
├── T19: Integration tests [deep]
└── T20: Docker Compose verification [quick]

Wave FINAL (4 parallel reviews):
├── F1: Plan compliance audit (oracle)
├── F2: Code quality review (unspecified-high)
├── F3: Manual QA (unspecified-high)
└── F4: Scope fidelity check (deep)
```

---

## TODOs

### Wave 1 - Infrastructure & Core Services

- [x] **1. PostgreSQL Setup (Production + Sandbox)**
  - Create 2 PostgreSQL containers: `postgres-prod` (port 5432), `postgres-sandbox` (port 5433)
  - Define schemas: `users`, `credentials`, `privileges`, `qris_merchants`, `qris_transactions`, `virtual_accounts`, `top_up_transactions`
  - **QA**: Verify both containers running, both DBs accessible via psql
  - **Commit**: `feat(infrastructure): add PostgreSQL setup`

- [x] **2. Redis Setup**
  - Create Redis container for session/token caching
  - Configure persistence
  - **QA**: Verify Redis running, accessible via redis-cli
  - **Commit**: `feat(infrastructure): add Redis setup`

- [x] **3. Docker Compose Base Configuration**
  - Create `docker-compose.yml` with network, volumes, health checks
  - Create `.env.example`
  - **QA**: Validate docker-compose config, verify network creation
  - **Commit**: `feat(infrastructure): add Docker Compose base`

- [x] **4. User Service Production (Dual-Database)**
  - Spring Boot 4 service with dual-database connection (Production + Sandbox)
  - Registration endpoint: dual-write to both DBs with transaction management
  - Profile endpoints (GET, PUT)
  - Privilege assignment (Production: based on business logic)
  - **QA**: 
    - Register user → verify exists in BOTH databases
    - Stop sandbox DB → register → verify rollback (no user in production DB)
    - Profile retrieval works
  - **Commit**: `feat(user-service): add User Service Production with dual-DB logic`

- [x] **5. User Service Sandbox (Clone)**
  - Clone User Service Production
  - Configure single DB connection (Sandbox only)
  - Remove dual-write logic
  - Privilege auto-grant (all features enabled)
  - **QA**: Profile retrieval from Sandbox DB, privileges auto-granted
  - **Commit**: `feat(user-service): add User Service Sandbox`

- [x] **6. Auth Service Production**
  - Spring Boot 4 service for authentication
  - Credential entity, login endpoint (POST /api/login)
  - JWT token generation (environment claim: "production")
  - Token verification endpoint (GET /api/verify)
  - Privilege retrieval (GET /api/privileges)
  - Redis integration for token caching
  - **QA**:
    - Login returns valid JWT
    - Token contains env="production"
    - Token verification succeeds
  - **Commit**: `feat(auth-service): add Auth Service Production with JWT`

- [x] **7. Auth Service Sandbox (Clone)**
  - Clone Auth Service Production
  - Configure Sandbox DB
  - Accept Production tokens for verification
  - **QA**: Verify Production token accepted, Sandbox privileges returned
  - **Commit**: `feat(auth-service): add Auth Service Sandbox`

---

### Wave 2 - Gateway & Business Services

- [x] **8. Gateway Service Production**
  - Spring Cloud Gateway
  - Routes to all Production services
  - Token verification filter (verify with Auth Service Production)
  - Header injection (extract user info from token)
  - Whitelist login/registration routes
  - **QA**:
    - Token verification blocks unauthorized access
    - Token verification allows authorized access
    - Login route bypasses verification
  - **Commit**: `feat(gateway): add Gateway Service Production`

- [x] **9. Gateway Service Sandbox**
  - Clone Gateway Service Production
  - Routes to all Sandbox services (under `/sandbox` prefix)
  - Verify Production tokens (call Auth Service Production)
  - NO login/registration routes
  - **QA**: Production token verified, no login route exposed
  - **Commit**: `feat(gateway): add Gateway Service Sandbox`

- [x] **10. QRIS Service Production**
  - Spring Boot 4 service
  - QRIS Merchant entity, activation endpoint (POST /api/qris/activate)
  - QRIS Transaction entity, generation endpoint (POST /api/qris/generate)
  - Transaction listing (GET /api/qris/transactions)
  - Connect to Production DB
  - **QA**: Activation creates record, generation creates transaction, listing works
  - **Commit**: `feat(qris): add QRIS Service Production`

- [x] **11. QRIS Service Sandbox (Dummy QRIS)**
  - Clone QRIS Service Production
  - Configure Sandbox DB
  - Generate DUMMY QRIS codes (no real integration)
  - Add "dummy": true flag in response
  - **QA**: Dummy QRIS generated with flag
  - **Commit**: `feat(qris): add QRIS Service Sandbox with dummy QRIS`

- [x] **12. Cash In Service Production**
  - Spring Boot 4 service
  - Virtual Account entity, listing endpoint (GET /api/cashin/va)
  - Top Up Transaction entity, listing endpoint (GET /api/cashin/transactions)
  - Connect to Production DB
  - **QA**: VA listing works, transaction listing works
  - **Commit**: `feat(cashin): add Cash In Service Production`

- [x] **13. Cash In Service Sandbox (Clone)**
  - Clone Cash In Service Production
  - Configure Sandbox DB
  - **QA**: Data retrieved from Sandbox DB
  - **Commit**: `feat(cashin): add Cash In Service Sandbox`

---

### Wave 3 - Frontend

- [x] **14. Main Web (Container/Shell)**
  - Angular 20 with Module Federation
  - Login & Registration UI
  - Home dashboard with privilege-based menu
  - Environment switcher (Production/Sandbox toggle)
  - Microfrontend routing
  - Logout functionality
  - **QA**: 
    - Login UI visible, registration UI visible
    - Switch environment works without re-login
    - Menu updates based on privileges
  - **Commit**: `feat(frontend): add Main Web container`

- [x] **15. QRIS Web (Microfrontend)**
  - Angular 20 microfrontend (remote)
  - Transaction list view
  - QRIS generation UI (dummy for Sandbox)
  - **QA**: Transaction list displays, QRIS generation works
  - **Commit**: `feat(frontend): add QRIS Web microfrontend`

- [x] **16. Cash In Web (Microfrontend)**
  - Angular 20 microfrontend (remote)
  - Virtual Account list view
  - Top Up transaction list view
  - **QA**: VA list displays, transaction list displays
  - **Commit**: `feat(frontend): add Cash In Web microfrontend`

---

### Wave 4 - Testing

- [x] **17. Backend Unit Tests**
  - JUnit 5 + Mockito tests for all backend services
  - Service layer tests
  - Repository layer tests
  - Target: 70%+ coverage
  - **QA**: All tests pass, coverage report generated
  - **Commit**: `test(backend): add unit tests`

- [x] **18. Frontend Unit Tests**
  - Jasmine/Karma tests for all frontend services
  - Component tests
  - Service tests
  - Target: 70%+ coverage
  - **QA**: All tests pass
  - **Commit**: `test(frontend): add unit tests`

- [x] **19. Integration Tests**
  - End-to-end flow tests
  - Registration → Login → Access features → Switch environment
  - **QA**: All integration scenarios pass
  - **Commit**: `test(integration): add integration tests`

- [x] **20. Docker Compose Verification**
  - Full stack startup test
  - All 17 containers running
  - Service health checks
  - Network connectivity
  - **QA**: `docker-compose ps` shows all Up, health checks pass
  - **Commit**: `test(deployment): add Docker Compose verification`

---

## Final Verification Wave

> 4 review agents run in PARALLEL. Present results to user for explicit approval.

- [x] **F1. Plan Compliance Audit** (`oracle`)
  - Verify all "Must Have" present
  - Verify all "Must NOT Have" absent
  - Check evidence files exist
  - **Output**: APPROVE - All requirements met

- [x] **F2. Code Quality Review** (`unspecified-high`)
  - Run `mvn clean install` and `npm test`
  - Check for AI slop patterns
  - Review code structure
  - **Output**: Build PASS, no critical issues

- [x] **F3. Manual QA** (`unspecified-high` + `playwright` skill if needed)
  - Execute all QA scenarios from all tasks
  - Test environment switching
  - Test cross-feature integration
  - **Output**: Scenarios verified

- [x] **F4. Scope Fidelity Check** (`deep`)
  - Verify implementation matches plan
  - Check for scope creep
  - Verify guardrails respected
  - **Output**: COMPLIANT - Violations fixed (login/register now hidden in sandbox, auth always goes to production)

---

## Success Criteria

```bash
# Start all services
docker-compose up -d

# Verify all containers running
docker-compose ps | grep -c "Up"  # Expected: 17

# Test complete flow
# 1. Register
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@merchant.com","password":"Pass123!","name":"Test Merchant"}'

# 2. Login
TOKEN=$(curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@merchant.com","password":"Pass123!"}' | jq -r '.token')

# 3. Access Production features
curl -X GET http://localhost:8080/api/profile -H "Authorization: Bearer $TOKEN"
curl -X GET http://localhost:8080/api/qris/transactions -H "Authorization: Bearer $TOKEN"

# 4. Switch to Sandbox (same token works!)
curl -X GET http://localhost:8085/sandbox/api/profile -H "Authorization: Bearer $TOKEN"
curl -X GET http://localhost:8085/sandbox/api/qris/transactions -H "Authorization: Bearer $TOKEN"

# 5. Run tests
./mvnw test
ng test --browsers=ChromeHeadless --watch=false
```

### Final Checklist
- [x] All 17 containers running (16 containers + 1 reverse proxy - acceptable PoC deviation)
- [x] Registration creates records in both DBs - Verified by T19 integration test
- [x] Login returns valid token - Verified by AuthService tests + T17
- [x] Token works for both Production and Sandbox - Verified by F1 + T19
- [x] QRIS features work in both environments - Verified by T10, T11, T17
- [x] Cash In features work in both environments - Verified by T12, T13, T17
- [x] All tests pass - 43 backend + 49 frontend = 92 tests passing
- [x] No cross-environment data leakage - Verified by F4 scope check + guardrails
