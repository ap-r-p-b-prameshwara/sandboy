# Code Quality Review Issues

## Review Date: 2026-07-13

---

## CRITICAL ISSUES

None found.

---

## HIGH PRIORITY ISSUES

### 1. Hardcoded Localhost URLs in Frontend
**Files:**
- `frontend/cashin-web/src/app/services/cashin.service.ts:9` - `private apiUrl = 'http://localhost:8080'`
- `frontend/qris-web/src/app/services/qris.service.ts:9` - `private apiUrl = 'http://localhost:8080'`
- `frontend/main-web/src/app/services/environment.service.ts:19-20` - hardcoded URLs

**Issue:** Frontend services have hardcoded localhost URLs that won't work in deployed environments.

**Recommendation:** Use Angular's environment configuration files consistently across all frontend apps.

---

## MEDIUM PRIORITY ISSUES

### 2. Inconsistent Lombok Usage
**Files:**
- `qris-service` DTOs and entities use manual getters/setters (verbose)
- `cashin-service` DTOs and entities use manual getters/setters (verbose)
- `user-service` and `auth-service` use Lombok @Data annotations

**Issue:** Inconsistent code style across services makes maintenance harder.

**Recommendation:** Standardize on Lombok across all services for consistency.

### 3. Verbose DTO Classes
**File:** `backend/qris-service/src/main/java/com/sandbox/qrisservice/dto/QrisResponse.java` (241 lines)

**Issue:** Contains multiple inner classes with manual getters/setters, making the file unnecessarily large.

**Recommendation:** Extract inner classes to separate files and use Lombok.

### 4. Inner Classes in Controllers
**Files:**
- `user-service/controller/UserController.java:57-62` - UpdateProfileRequest
- `gateway/filter/TokenVerificationFilter.java:53-61` - VerifyResponse

**Issue:** Inner classes for request/response DTOs should be separate files for reusability.

**Recommendation:** Move to dedicated DTO files.

### 5. Generic Exception Handling
**Files:** All backend services

**Issue:** Using generic `RuntimeException` instead of custom business exceptions.

**Recommendation:** Create custom exception classes and a global exception handler.

---

## LOW PRIORITY ISSUES

### 6. console.error in Frontend
**Files:**
- `frontend/cashin-web/src/app/components/va-list/va-list.component.ts:53`
- `frontend/qris-web/src/app/components/transaction-list/transaction-list.component.ts:53`
- `frontend/main-web/src/app/components/login/login.component.ts:42`
- `frontend/main-web/src/app/components/register/register.component.ts:43`

**Issue:** console.error statements in production code.

**Recommendation:** Implement proper logging service or remove for production builds.

### 7. Inconsistent Controller Injection Style
**Files:**
- `qris-service/controller/QrisController.java` - uses constructor injection manually
- `cashin-service/controller/CashInController.java` - uses constructor injection manually

**Issue:** user-service and auth-service use @RequiredArgsConstructor, but qris-service and cashin-service use manual constructor.

**Recommendation:** Standardize on @RequiredArgsConstructor for consistency.

---

## POSITIVE FINDINGS

1. **No hardcoded secrets** - All credentials use environment variables
2. **No TODO/FIXME comments** left in code
3. **No unused imports** detected
4. **No placeholder/throwaway code** found
5. **Proper validation** with Jakarta annotations
6. **Clean package structure** following conventions
7. **Proper layering** (controller, service, repository, entity, dto)
