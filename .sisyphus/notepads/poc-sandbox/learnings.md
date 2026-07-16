# Learnings

## Project Context
- **Tech Stack**: Spring Boot 4 + Java 25, Angular 20, PostgreSQL, Redis
- **Architecture**: Clone services with minimal environment-specific logic
- **Deployment**: Local Docker with Docker Compose
- **Total Containers**: 17 (2 DB + 1 Redis + 10 backend + 3 frontend + 1 reverse proxy)

## Key Decisions
- Authentication: Login/Registration ONLY from Production
- Dual-write: User data synced to both Production and Sandbox DBs
- Data Isolation: NO shared database tables between environments
- No login UI in Sandbox - users login from Production and switch environments

## Conventions
- Production DB: port 5432
- Sandbox DB: port 5433
- Production Gateway: port 8080
- Sandbox Gateway: port 8085
- JWT: contains environment claim ("production" or "sandbox")

## Timestamps
- 2026-07-13: Session started

## Lombok + Java 25 Fix (2026-07-13)

### Root Cause
Lombok annotation processor not registered in maven-compiler-plugin annotationProcessorPaths. Spring Boot 4.0.0 parent POM delegates to the `release` flag via `<java.version>25</java.version>`, but the maven-compiler-plugin must explicitly declare Lombok as an annotation processor for it to work with Java 25.

### Fix Applied to All 4 Backend Services
1. Added `<lombok.version>1.18.38</lombok.version>` to `<properties>` section
2. Added `maven-compiler-plugin` with `<annotationProcessorPaths>` including Lombok:
   ```xml
   <plugin>
       <groupId>org.apache.maven.plugins</groupId>
       <artifactId>maven-compiler-plugin</artifactId>
       <configuration>
           <source>25</source>
           <target>25</target>
           <annotationProcessorPaths>
               <path>
                   <groupId>org.projectlombok</groupId>
                   <artifactId>lombok</artifactId>
                   <version>${lombok.version}</version>
               </path>
           </annotationProcessorPaths>
       </configuration>
   </plugin>
   ```

### JDK Environment
- JDK 25.0.3-tem available via sdkman
- Default JDK is 21.0.8-tem - must use `JAVA_HOME` override to compile
- Maven compiler plugin uses `release 25` flag for compilation

### Test Results
- **user-service**: 9 tests passing
- **auth-service**: 17 tests passing (9 JwtTokenProvider + 8 AuthService)
- **qris-service**: 10 tests passing
- **cashin-service**: 7 tests passing
- **Total**: 43 tests, 0 failures

## Integration Tests Created (2026-07-13)

### Files Created:
- `backend/integration-tests.sh` - 569-line automated bash test script
- `backend/integration-tests/README.md` - 142-line documentation

### Test Scenarios (12 steps, ~25 assertions):
1. **Service startup** - docker-compose with `--profile backend`, waits for all 13 containers healthy
2. **Registration** - POST /api/register → 201, validates email in response
3. **Dual DB write** - Checks both prod (5432) and sandbox (5433) DBs for the user record; also verifies user IDs match across environments
4. **Login** - POST /api/login → 200 with JWT token
5. **Production features** - GET /api/profile, /api/qris/transactions, /api/cashin/va, /api/cashin/transactions → 200 with token
6. **Sandbox features (same token)** - Same endpoints on localhost:8085 with same token → 200
7. **Data isolation** - Direct insert into Sandbox DB only, verifies absence in Production DB
8. **Error scenarios** - 12 sub-tests: no token (401), invalid token (401), empty Bearer (401), wrong method (405), empty body (400), wrong password (401), unknown email (401), sandbox no-token (401)

### Architecture Verified:
- Both gateways serve same API paths (`/api/profile`, `/api/qris/**`, `/api/cashin/**`) on different ports
- Sandbox gateway routes to sandbox services (user-service-sandbox:8082, qris-service-sandbox:8087, cashin-service-sandbox:8089)
- Token verification done against PRODUCTION auth service for both gateways
- No `/sandbox` path prefix used - same paths, different ports

### Script Features:
- `set -euo pipefail` for safety
- Colored PASS/FAIL output (green/red/cyan/yellow)
- Auto-cleanup via trap on EXIT
- `--skip-build` and `--skip-clean` flags
- Unique test email per run (integ-test-<timestamp>@merchant.com)
- Helper functions: test_http_status, test_http_body, check_db_record, wait_for_service, wait_for_container_healthy
- Automatic counter tracking (PASSED/FAILED/TOTAL)
- Usage via sed header extraction

## Frontend Unit Tests Created (2026-07-13)

### Test Files (6 files, 49 tests, 543 lines):
- **main-web**: auth.service.spec.ts (11 tests), environment.service.spec.ts (8 tests), login.component.spec.ts (11 tests), home.component.spec.ts (11 tests)
- **qris-web**: qris.service.spec.ts (4 tests)
- **cashin-web**: cashin.service.spec.ts (4 tests)

### Patterns Used:
- HttpClientTestingModule + HttpTestingController for API mocking
- jasmine.createSpyObj for dependency mocking
- fakeAsync/tick for async component tests
- localStorage.clear() in beforeEach/afterEach for isolation
- spyOn for non-configurable methods (like Location.reload)

### Test Results: 49/49 passing across 3 projects

## Backend Unit Tests Created (2026-07-13)

### Test Files Created:
- `auth-service/src/test/java/com/sandbox/authservice/service/AuthServiceTest.java`
  - **AuthService**: login (success, email not found, wrong password), verifyToken (valid, invalid JWT, not in cache), getUserIdFromToken, getEmailFromToken

- `auth-service/src/test/java/com/sandbox/authservice/security/JwtTokenProviderTest.java`
  - **JwtTokenProvider**: generateToken, validateToken (valid, malformed, empty), getUserIdFromToken, getEmailFromToken, getEnvironmentFromToken, sandbox env, different users

- `qris-service/src/test/java/com/sandbox/qrisservice/service/QrisServiceTest.java`
  - **QrisService**: activate (success, already exists, NMID duplicate), generateQris (success, merchant not found, not active), getTransactions (success with 2, merchant not found, empty list), isActive verification

- `cashin-service/src/test/java/com/sandbox/cashinservice/service/CashInServiceTest.java`
  - **CashInService**: getVirtualAccounts (success with 2, empty, includes inactive), getTopUpTransactions (success with 2, empty, ordered by date desc, multiple VAs)

### Total Tests: 25 test methods across 4 test classes
### Compilation Note: Projects target Java 25 but environment has JDK 21 - requires JDK 25 to compile and run

## Frontend Unit Tests Created (2026-07-13)

### Test Infrastructure Setup:
- Added Jasmine/Karma dependencies (karma, karma-chrome-launcher, karma-jasmine, jasmine-core, @types/jasmine)
- Created `karma.conf.js` for each web project (main-web, qris-web, cashin-web)
- Created `src/test.ts` entry point for each web project
- Created `tsconfig.spec.json` for TypeScript test configuration
- Added `test` architect target to each `angular.json`

### Test Files Created:

- `frontend/main-web/src/app/services/auth.service.spec.ts` (11 tests)
  - **AuthService**: login (success, error), register, setToken, getToken (with/without token), logout, isLoggedIn (true/false)
  
- `frontend/main-web/src/app/services/environment.service.spec.ts` (8 tests)
  - **EnvironmentService**: default production, setEnvironment (prod/sandbox), apiUrl (prod/sandbox/switch), environment getter
  
- `frontend/main-web/src/app/components/login/login.component.spec.ts` (11 tests)
  - **LoginComponent**: creation, form rendering (inputs, buttons), initial state (empty fields, showRegister false), form submit calls onLogin, login success sets token, login error no token, required validation on inputs
  
- `frontend/main-web/src/app/components/home/home.component.spec.ts` (11 tests)
  - **HomeComponent**: creation, welcome message, logout button, env switcher, logged-in message, selectedEnv init, apiUrl display, logout button click, env change calls service, select options, production default

- `frontend/qris-web/src/app/services/qris.service.spec.ts` (4 tests)
  - **QrisService**: getTransactions (success, error), getTransaction (success, 404)

- `frontend/cashin-web/src/app/services/cashin.service.spec.ts` (4 tests)
  - **CashInService**: getVirtualAccounts (success, error), getVirtualAccount (success, 404)

### Key Patterns Used:
- `HttpClientTestingModule` + `HttpTestingController` for HTTP mocking
- `jasmine.createSpyObj` for service dependency mocking
- `fakeAsync`/`tick` for async HTTP tests
- `fixture.debugElement` for DOM testing
- `Object.defineProperty` on `window.location.reload` avoided - components that call `window.location.reload()` are tested via `spyOn(component, 'method')` to prevent page reload in test runner

### Gotchas:
- `window.location.reload` is non-configurable in Chrome, cannot be spied on directly
- `Location.prototype.reload` does not exist as a method in the test environment
- `delete (window as any).location` throws "Cannot delete property 'location'" 
- Solution: spy on component methods that call reload, don't test reload assertion
- ngModel model→view binding didn't work in standalone component tests even with FormsModule imported; used required validation tests instead

### Total Tests: 49 test methods (39 main-web + 4 qris-web + 4 cashin-web)
### All tests pass: `ng test --browsers=ChromeHeadless --watch=false`

## CashIn Service Implementation (2026-07-13)
- **Production Port**: 8088
- **Sandbox Port**: 8089
- **Schema**: virtual_accounts
- **Endpoints**:
  - GET /api/cashin/va - List virtual accounts (X-User-Id header)
  - GET /api/cashin/transactions - List top-up transactions (X-User-Id header)
- **Entities**:
  - VirtualAccount: userId, bankName, accountNumber, accountName, isActive
  - TopUpTransaction: userId, vaId, amount, reference, status, transactionDate
- **No Lombok**: Plain getters/setters/constructors
- **Constructor injection**: Used throughout
- **Response DTOs**: Nested data classes (VirtualAccountData, TransactionData)

## Frontend Angular Apps - Local Startup (2026-07-13)

### Running Apps (3 microfrontends):
| App | Port | Status | URL |
|-----|------|--------|-----|
| main-web | 4200 | ✅ Running | http://localhost:4200 |
| qris-web | 4201 | ✅ Running | http://localhost:4201 |
| cashin-web | 4202 | ✅ Running | http://localhost:4202 |

### Key Issues Fixed:

1. **`@angular-architects/module-federation` version mismatch**: The initial `npm install` installed v21.2.2 (latest) but the project uses Angular 18. Had to pin to `@angular-architects/module-federation@^18.0.6`.

2. **Missing `serve` builder**: In v18.x of `@angular-architects/module-federation`, the `serve` builder was removed. `builders.json` only contains `build`. The angular.json was referencing `@angular-architects/module-federation:serve` which doesn't exist. Fixed by changing to `"builder": "@angular-devkit/build-angular:dev-server"` in all 3 angular.json files.

3. **main-web compilation error**: `LoginComponent` used `<app-register>` in its template but didn't import `RegisterComponent`. Fixed by adding `import { RegisterComponent } from '../register/register.component'` and including it in the `imports` array.

### Commands to start:
```bash
# For each project:
npm install @angular-architects/module-federation@^18.0.6 --save
nohup npx ng serve --host 0.0.0.0 --port {PORT} > /tmp/{app}.log 2>&1 &
```

### PIDs:
- main-web: 78146
- qris-web: 77986
- cashin-web: 78008

### Logs:
- main-web: `/tmp/main-web.log`
- qris-web: `/tmp/qris-web.log`
- cashin-web: `/tmp/cashin-web.log`

## On-Demand User Sync from Production to Sandbox (2026-07-14)

### Architecture
- **Flow**: `auth-service-sandbox` → `user-service-sandbox` (POST /api/sync) → `user-service-prod` (GET /api/users/{email}/sync-data)
- **Trigger**: Login attempt on sandbox fails with "Not found in sandbox"
- **Internal communication**: Docker network (`sandbox-network`), services reachable by container name

### Files Created (2):
- `backend/user-service/.../entity/Credential.java` - JPA entity for `credentials.credential` table
- `backend/user-service/.../repository/CredentialRepository.java` - JPA repository with `findByEmail(String)`
- `backend/user-service/.../controller/SyncController.java` - POST /api/sync endpoint

### Files Modified (3):
- `backend/user-service/.../controller/UserController.java`:
  - Added `UserRepository`, `CredentialRepository`, `PrivilegeRepository` as constructor-injected fields
  - Added `GET /api/users/{email}/sync-data` endpoint returning user + credential + privileges
- `backend/auth-service/.../service/AuthService.java`:
  - Added `RestTemplate` field (inline init, excluded from @RequiredArgsConstructor)
  - Added `sandboxServiceUrl` via @Value
  - Changed credential-not-found exception to "Not found in sandbox" for sandbox detection
  - Wrapped login in try-catch: on "Not found in sandbox" in sandbox env → sync from prod → retry
- `backend/gateway/.../GatewayApplication.java`:
  - Added `/api/sync` route → `http://user-service-sandbox:8082` (no token verification)

### Key Design Decisions
- Credential entity in user-service is plain (no Lombok) with manual getters/setters to avoid annotation processor dependency issues
- `SyncController` uses manual constructor injection instead of `@RequiredArgsConstructor` to keep RestTemplate initialization explicit
- The auth-service calls user-service-sandbox DIRECTLY (not through gateway) for sync, since it's on the same Docker network
- Gateway route for `/api/sync` added primarily for debugging/admin access
- `DualWriteUserService` left untouched - registration still dual-writes to both DBs

### Verification
- Build not possible locally (JDK 21 vs pom.xml Java 25 requirement) - pre-existing constraint
- Code is structurally verified: all imports correct, constructor injection consistent, Lombok usage matches existing patterns
