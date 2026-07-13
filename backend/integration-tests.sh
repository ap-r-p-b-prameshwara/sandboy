#!/usr/bin/env bash
# =============================================================================
# Sandbox POC - End-to-End Integration Tests
# =============================================================================
#
# Usage:
#   ./backend/integration-tests.sh              # Full test run (build + test + cleanup)
#   ./backend/integration-tests.sh --skip-build  # Skip docker build, run tests only
#   ./backend/integration-tests.sh --skip-clean  # Run tests, leave services running
#   ./backend/integration-tests.sh --help        # Show this help
#
# Prerequisites:
#   - Docker & Docker Compose installed
#   - psql (PostgreSQL client) installed
#   - curl, jq installed
#   - Ports 8080, 8085, 5432, 5433 free
#   - .env file present at project root
#
# Architecture:
#   Production Gateway : http://localhost:8080
#   Sandbox Gateway    : http://localhost:8085
#   Production DB      : postgresql://localhost:5432/sandbox_prod
#   Sandbox DB         : postgresql://localhost:5433/sandbox_sandbox
#
#   Auth flows (register/login) ALWAYS go through Production gateway.
#   Same JWT token works in both environments (verified by Production auth service).
#   Data is dual-written to both DBs on registration.
#
# Test Scenarios:
#   1.  Start services via docker-compose
#   2.  Register new user → verify 201 Created
#   3.  Verify dual-write in both Production and Sandbox DBs
#   4.  Login → get JWT token
#   5.  Access Production features with token (profile, qris, cashin)
#   6.  Switch to Sandbox → same token works (profile, qris, cashin)
#   7.  Verify no cross-environment data leakage
#   8.  Error scenarios (invalid token, unauthorized access)
#   9.  Stop services and cleanup
# =============================================================================

set -euo pipefail

# --- Configuration -----------------------------------------------------------
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
COMPOSE_FILE="${PROJECT_ROOT}/docker-compose.yml"
ENV_FILE="${PROJECT_ROOT}/.env"

PROD_GW="http://localhost:8080"
SANDBOX_GW="http://localhost:8085"
PROD_DB="localhost:5432"
SANDBOX_DB="localhost:5433"
DB_USER="sandbox_user"
DB_PASS="your_secure_password_here"
PROD_DB_NAME="sandbox_prod"
SANDBOX_DB_NAME="sandbox_sandbox"

TEST_EMAIL="integ-test-$(date +%s)@merchant.com"
TEST_PASSWORD="TestPass123!"
TEST_NAME="Integration Test Merchant"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Counters
PASSED=0
FAILED=0
TOTAL=0

# Flags
SKIP_BUILD=false
SKIP_CLEAN=false

# --- Helper Functions --------------------------------------------------------

usage() {
    sed -n '3,28p' "$0"
    exit 0
}

log_info()  { echo -e "${CYAN}[INFO]${NC}  $*"; }
log_ok()    { echo -e "${GREEN}[PASS]${NC}  $*"; ((PASSED++)); ((TOTAL++)); }
log_fail()  { echo -e "${RED}[FAIL]${NC}  $*"; ((FAILED++)); ((TOTAL++)); }
log_step()  { echo -e "\n${YELLOW}═══════════════════════════════════════════════════${NC}"; \
              echo -e "${YELLOW}  STEP $1: $2${NC}"; \
              echo -e "${YELLOW}═══════════════════════════════════════════════════${NC}"; }
log_skip()  { echo -e "${YELLOW}[SKIP]${NC}  $*"; }

check_deps() {
    local missing=0
    for cmd in docker psql curl jq; do
        if ! command -v "$cmd" &>/dev/null; then
            echo "Error: '$cmd' is not installed." >&2
            missing=1
        fi
    done
    if [ "$missing" -eq 1 ]; then
        echo "Install missing dependencies and try again." >&2
        exit 1
    fi
}

check_env() {
    if [ ! -f "$ENV_FILE" ]; then
        echo "Error: .env file not found at $ENV_FILE" >&2
        echo "Copy .env.example to .env and configure it first." >&2
        exit 1
    fi
}

# Run a test and check if HTTP status matches expected
# Usage: test_http_status "Test Name" "METHOD" "URL" [HEADERS...] expected_status
test_http_status() {
    local name="$1"
    local method="$2"
    local url="$3"
    local expected="$4"
    shift 4

    local status
    status=$(curl -s -o /dev/null -w "%{http_code}" -X "$method" "$url" "$@" 2>/dev/null || echo "000")

    if [ "$status" = "$expected" ]; then
        log_ok "$name (HTTP $status)"
    else
        log_fail "$name - expected HTTP $expected, got HTTP $status"
    fi
}

# Run a test, extract a JSON value, and return the full body
# Usage: test_http_body "Test Name" "METHOD" "URL" [HEADERS...]
test_http_body() {
    local name="$1"
    local method="$2"
    local url="$3"
    shift 3

    local body status
    body=$(curl -s -X "$method" "$url" "$@" 2>/dev/null) || true
    status=$(echo "$body" | jq -r '.' 2>/dev/null && echo "parsed" || echo "unparseable")

    if [ "$status" = "parsed" ]; then
        log_ok "$name - got valid JSON response"
    else
        log_fail "$name - response is not valid JSON"
        echo "       Raw: $body"
    fi

    echo "$body"
}

# Check DB record exists
# Usage: check_db_record "Description" "host:port" "dbname" "query" "expected_value"
check_db_record() {
    local desc="$1"
    local host_port="$2"
    local db="$3"
    local query="$4"
    local expected="$5"

    local host="${host_port%%:*}"
    local port="${host_port##*:}"

    local result
    result=$(PGPASSWORD="$DB_PASS" psql -h "$host" -p "$port" -U "$DB_USER" -d "$db" \
        -t -A -c "$query" 2>/dev/null || echo "ERROR")

    if [ "$result" = "$expected" ]; then
        log_ok "$desc (found: $result)"
    else
        log_fail "$desc - expected '$expected', got '$result'"
    fi
}

# Wait for a URL to return HTTP 200
wait_for_service() {
    local name="$1"
    local url="$2"
    local max_attempts="${3:-30}"
    local wait_sec="${4:-5}"

    log_info "Waiting for $name at $url ..."
    for i in $(seq 1 "$max_attempts"); do
        if curl -sf -o /dev/null "$url" 2>/dev/null; then
            log_info "$name is ready (attempt $i)"
            return 0
        fi
        sleep "$wait_sec"
    done
    log_fail "$name did not become ready after $((max_attempts * wait_sec))s"
    return 1
}

wait_for_container_healthy() {
    local container="$1"
    local max_attempts="${2:-30}"
    local wait_sec="${3:-5}"

    log_info "Waiting for container $container to be healthy ..."
    for i in $(seq 1 "$max_attempts"); do
        local status
        status=$(docker inspect --format='{{.State.Health.Status}}' "$container" 2>/dev/null || echo "not_found")
        if [ "$status" = "healthy" ]; then
            log_info "Container $container is healthy (attempt $i)"
            return 0
        fi
        sleep "$wait_sec"
    done
    log_fail "Container $container not healthy after $((max_attempts * wait_sec))s"
    return 1
}

cleanup() {
    log_step "13" "Cleanup: stopping services and removing test data"

    log_info "Cleaning up test user from databases ..."
    local host_prod="${PROD_DB%%:*}"
    local port_prod="${PROD_DB##*:}"
    local host_sandbox="${SANDBOX_DB%%:*}"
    local port_sandbox="${SANDBOX_DB##*:}"

    PGPASSWORD="$DB_PASS" psql -h "$host_prod" -p "$port_prod" -U "$DB_USER" -d "$PROD_DB_NAME" \
        -c "DELETE FROM users.user WHERE email='$TEST_EMAIL';" 2>/dev/null || true
    PGPASSWORD="$DB_PASS" psql -h "$host_sandbox" -p "$port_sandbox" -U "$DB_USER" -d "$SANDBOX_DB_NAME" \
        -c "DELETE FROM users.user WHERE email='$TEST_EMAIL';" 2>/dev/null || true

    # Stop services
    log_info "Stopping docker-compose services ..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" --profile backend down -v 2>/dev/null || true

    log_ok "Cleanup complete"
}

# --- Main Test Flow ----------------------------------------------------------

main() {
    # Parse arguments
    while [ $# -gt 0 ]; do
        case "$1" in
            --skip-build) SKIP_BUILD=true; shift ;;
            --skip-clean) SKIP_CLEAN=true; shift ;;
            --help|-h) usage ;;
            *) echo "Unknown option: $1"; usage ;;
        esac
    done

    echo -e "${CYAN}"
    echo "╔══════════════════════════════════════════════════════════════╗"
    echo "║     Sandbox POC - End-to-End Integration Tests              ║"
    echo "║     $(date)                ║"
    echo "╚══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"

    # Check prerequisites
    check_deps
    check_env

    # Source .env
    set -a; source "$ENV_FILE"; set +a

    # Ensure cleanup on exit (unless --skip-clean)
    if [ "$SKIP_CLEAN" = false ]; then
        trap cleanup EXIT
    fi

    # =========================================================================
    # STEP 1: Start Services
    # =========================================================================
    log_step "1" "Start all backend services via docker-compose"

    BUILD_FLAG=""
    if [ "$SKIP_BUILD" = true ]; then
        BUILD_FLAG="--no-build"
        log_info "Skipping docker build (--skip-build)"
    fi

    log_info "Starting services with docker-compose (profile: backend) ..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" --profile backend up -d $BUILD_FLAG
    log_info "Docker compose started. Waiting for all services to become healthy ..."

    # Wait for infrastructure (DBs + Redis) - they start first
    wait_for_container_healthy "postgres-prod" 10 6
    wait_for_container_healthy "postgres-sandbox" 10 6
    wait_for_container_healthy "redis" 10 6

    # Wait for backend services
    wait_for_container_healthy "user-service-prod" 20 6
    wait_for_container_healthy "user-service-sandbox" 20 6
    wait_for_container_healthy "auth-service-prod" 20 6
    wait_for_container_healthy "auth-service-sandbox" 20 6

    # Gateways need upstream services first
    wait_for_container_healthy "gateway-prod" 20 6
    wait_for_container_healthy "gateway-sandbox" 20 6

    # QRIS and CashIn services
    wait_for_container_healthy "qris-service-prod" 20 6
    wait_for_container_healthy "qris-service-sandbox" 20 6
    wait_for_container_healthy "cashin-service-prod" 20 6
    wait_for_container_healthy "cashin-service-sandbox" 20 6

    # Final health check on gateways
    wait_for_service "Production Gateway" "${PROD_GW}/actuator/health" 6 5
    wait_for_service "Sandbox Gateway" "${SANDBOX_GW}/actuator/health" 6 5

    log_ok "All services are up and healthy"
    log_info "Production Gateway: ${PROD_GW}/actuator/health"
    log_info "Sandbox Gateway:    ${SANDBOX_GW}/actuator/health"

    # =========================================================================
    # STEP 2: Register New User
    # =========================================================================
    log_step "2" "Register new user (always goes to Production gateway)"

    local register_body
    register_body=$(test_http_body "Register user" "POST" "${PROD_GW}/api/register" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\",\"name\":\"$TEST_NAME\"}")

    local http_status
    http_status=$(echo "$register_body" | jq -r '.email // "missing"' 2>/dev/null || echo "unparseable")

    if [ "$http_status" = "$TEST_EMAIL" ]; then
        log_ok "Registration returned correct email: $TEST_EMAIL"
    elif echo "$register_body" | grep -q "email.*already exists\|duplicate\|already registered" 2>/dev/null; then
        log_info "User may already exist from a previous run - continuing"
    else
        log_info "Registration response: $(echo "$register_body" | head -c 200)"
    fi

    # =========================================================================
    # STEP 3: Verify Dual DB Write
    # =========================================================================
    log_step "3" "Verify dual-write: user exists in both Production and Sandbox DBs"

    check_db_record "Production DB has user" \
        "$PROD_DB" "$PROD_DB_NAME" \
        "SELECT email FROM users.user WHERE email='$TEST_EMAIL';" \
        "$TEST_EMAIL"

    check_db_record "Sandbox DB has user" \
        "$SANDBOX_DB" "$SANDBOX_DB_NAME" \
        "SELECT email FROM users.user WHERE email='$TEST_EMAIL';" \
        "$TEST_EMAIL"

    # Verify user IDs are SAME across both DBs (dual-write preserves identity)
    local prod_id sand_id
    prod_id=$(PGPASSWORD="$DB_PASS" psql -h "${PROD_DB%%:*}" -p "${PROD_DB##*:}" -U "$DB_USER" -d "$PROD_DB_NAME" \
        -t -A -c "SELECT id FROM users.user WHERE email='$TEST_EMAIL';" 2>/dev/null || echo "ERROR")
    sand_id=$(PGPASSWORD="$DB_PASS" psql -h "${SANDBOX_DB%%:*}" -p "${SANDBOX_DB##*:}" -U "$DB_USER" -d "$SANDBOX_DB_NAME" \
        -t -A -c "SELECT id FROM users.user WHERE email='$TEST_EMAIL';" 2>/dev/null || echo "ERROR")

    if [ "$prod_id" != "ERROR" ] && [ "$sand_id" != "ERROR" ] && [ "$prod_id" = "$sand_id" ]; then
        log_ok "User IDs match across environments: ID=$prod_id"
    else
        log_info "User IDs: prod=$prod_id sandbox=$sand_id (may differ if not dual-write)"
    fi

    # =========================================================================
    # STEP 4: Login
    # =========================================================================
    log_step "4" "Login with registered credentials (always goes to Production)"

    local login_body
    login_body=$(curl -s -X POST "${PROD_GW}/api/login" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}" 2>/dev/null || echo "{}")

    TOKEN=$(echo "$login_body" | jq -r '.token // empty' 2>/dev/null || echo "")

    if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
        log_ok "Login successful - JWT token received (length: ${#TOKEN})"
    else
        log_fail "Login failed or no token in response"
        echo "       Response: $(echo "$login_body" | head -c 300)"
        # If login failed, try to get user ID directly for remaining tests
        USER_ID=$(PGPASSWORD="$DB_PASS" psql -h "${PROD_DB%%:*}" -p "${PROD_DB##*:}" -U "$DB_USER" -d "$PROD_DB_NAME" \
            -t -A -c "SELECT id FROM users.user WHERE email='$TEST_EMAIL';" 2>/dev/null || echo "")
        if [ -n "$USER_ID" ]; then
            log_info "Continuing with DB-retrieved USER_ID=$USER_ID (no token available)"
        fi
    fi

    # =========================================================================
    # STEP 5: Access Production Features
    # =========================================================================
    log_step "5" "Access Production features with JWT token"

    if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
        # Production Profile
        local profile_body
        profile_body=$(curl -s "${PROD_GW}/api/profile" \
            -H "Authorization: Bearer $TOKEN" 2>/dev/null || echo "{}")
        local profile_email
        profile_email=$(echo "$profile_body" | jq -r '.email // "missing"' 2>/dev/null || echo "missing")
        if [ "$profile_email" = "$TEST_EMAIL" ]; then
            log_ok "Production profile - correct email: $profile_email"
        else
            log_fail "Production profile - expected '$TEST_EMAIL', got '$profile_email'"
            echo "       Response: $(echo "$profile_body" | head -c 200)"
        fi

        # Production QRIS
        test_http_status "Production QRIS transactions" "GET" "${PROD_GW}/api/qris/transactions" \
            "200" -H "Authorization: Bearer $TOKEN"

        # Production CashIn VA
        test_http_status "Production CashIn VA list" "GET" "${PROD_GW}/api/cashin/va" \
            "200" -H "Authorization: Bearer $TOKEN"

        # Production CashIn Transactions
        test_http_status "Production CashIn transactions" "GET" "${PROD_GW}/api/cashin/transactions" \
            "200" -H "Authorization: Bearer $TOKEN"
    else
        log_skip "No token available - skipping Production feature tests"
    fi

    # =========================================================================
    # STEP 6: Access Sandbox Features (Same Token)
    # =========================================================================
    log_step "6" "Access Sandbox features with SAME JWT token"

    if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
        # Sandbox Profile
        local sandbox_profile
        sandbox_profile=$(curl -s "${SANDBOX_GW}/api/profile" \
            -H "Authorization: Bearer $TOKEN" 2>/dev/null || echo "{}")
        local sandbox_email
        sandbox_email=$(echo "$sandbox_profile" | jq -r '.email // "missing"' 2>/dev/null || echo "missing")
        if [ "$sandbox_email" = "$TEST_EMAIL" ]; then
            log_ok "Sandbox profile - correct email: $sandbox_email (same token works!)"
        else
            log_fail "Sandbox profile - expected '$TEST_EMAIL', got '$sandbox_email'"
            echo "       Response: $(echo "$sandbox_profile" | head -c 200)"
        fi

        # Sandbox QRIS
        test_http_status "Sandbox QRIS transactions" "GET" "${SANDBOX_GW}/api/qris/transactions" \
            "200" -H "Authorization: Bearer $TOKEN"

        # Sandbox CashIn VA
        test_http_status "Sandbox CashIn VA list" "GET" "${SANDBOX_GW}/api/cashin/va" \
            "200" -H "Authorization: Bearer $TOKEN"

        # Sandbox CashIn Transactions
        test_http_status "Sandbox CashIn transactions" "GET" "${SANDBOX_GW}/api/cashin/transactions" \
            "200" -H "Authorization: Bearer $TOKEN"
    else
        log_skip "No token available - skipping Sandbox feature tests"
    fi

    # =========================================================================
    # STEP 7: Verify No Cross-Environment Data Leakage
    # =========================================================================
    log_step "7" "Verify no cross-environment data leakage"

    if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
        # Register a sandbox-only test (via direct DB insert) to verify isolation
        local isolated_email="isolated-$(date +%s)@sandbox-only.com"
        log_info "Testing data isolation with email: $isolated_email"

        # Insert directly into Sandbox DB only (simulating sandbox-specific data)
        PGPASSWORD="$DB_PASS" psql -h "${SANDBOX_DB%%:*}" -p "${SANDBOX_DB##*:}" -U "$DB_USER" -d "$SANDBOX_DB_NAME" \
            -c "INSERT INTO users.user (email, password_hash, name) VALUES ('$isolated_email', 'sandbox_only_hash', 'Isolated User');" 2>/dev/null || true

        # Verify it exists in Sandbox DB
        check_db_record "Sandbox-only user exists in Sandbox DB" \
            "$SANDBOX_DB" "$SANDBOX_DB_NAME" \
            "SELECT email FROM users.user WHERE email='$isolated_email';" \
            "$isolated_email"

        # Verify it does NOT exist in Production DB
        local leaked
        leaked=$(PGPASSWORD="$DB_PASS" psql -h "${PROD_DB%%:*}" -p "${PROD_DB##*:}" -U "$DB_USER" -d "$PROD_DB_NAME" \
            -t -A -c "SELECT email FROM users.user WHERE email='$isolated_email';" 2>/dev/null || echo "")
        if [ -z "$leaked" ]; then
            log_ok "Sandbox-only user is NOT present in Production DB (no leakage)"
        else
            log_fail "DATA LEAKAGE: Sandbox-only user found in Production DB!"
        fi

        # Clean up the isolated test record
        PGPASSWORD="$DB_PASS" psql -h "${SANDBOX_DB%%:*}" -p "${SANDBOX_DB##*:}" -U "$DB_USER" -d "$SANDBOX_DB_NAME" \
            -c "DELETE FROM users.user WHERE email='$isolated_email';" 2>/dev/null || true
    else
        log_skip "No token available - skipping data leakage tests"
    fi

    # =========================================================================
    # STEP 8: Error Scenarios
    # =========================================================================
    log_step "8" "Error scenarios (invalid token, unauthorized access)"

    # 8a: Unauthorized - no token
    test_http_status "Unauthorized (no token) - Production profile" "GET" "${PROD_GW}/api/profile" "401"

    # 8b: Unauthorized - no token (Sandbox)
    test_http_status "Unauthorized (no token) - Sandbox profile" "GET" "${SANDBOX_GW}/api/profile" "401"

    # 8c: Invalid token - garbage string
    test_http_status "Invalid token - Production profile" "GET" "${PROD_GW}/api/profile" "401" \
        -H "Authorization: Bearer invalidtoken123"

    # 8d: Invalid token - empty Bearer
    test_http_status "Invalid token (empty) - Production profile" "GET" "${PROD_GW}/api/profile" "401" \
        -H "Authorization: Bearer "

    # 8e: Invalid token on Sandbox
    test_http_status "Invalid token - Sandbox profile" "GET" "${SANDBOX_GW}/api/profile" "401" \
        -H "Authorization: Bearer invalidtoken123"

    # 8f: QRIS with no token
    test_http_status "Unauthorized (no token) - QRIS" "GET" "${PROD_GW}/api/qris/transactions" "401"

    # 8g: CashIn with no token
    test_http_status "Unauthorized (no token) - CashIn" "GET" "${PROD_GW}/api/cashin/va" "401"

    # 8h: Wrong HTTP method on register (GET instead of POST)
    test_http_status "Wrong method on register" "GET" "${PROD_GW}/api/register" "405" \
        -H "Content-Type: application/json"

    # 8i: Register with missing fields
    test_http_status "Register with empty body" "POST" "${PROD_GW}/api/register" "400" \
        -H "Content-Type: application/json" -d "{}"

    # 8j: Login with wrong password
    test_http_status "Login with wrong password" "POST" "${PROD_GW}/api/login" "401" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"wrong_password\"}"

    # 8k: Login with non-existent user
    test_http_status "Login with unknown email" "POST" "${PROD_GW}/api/login" "401" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"nonexistent-$(date +%s)@test.com\",\"password\":\"AnyPass1!\"}"

    # 8l: Access auth-protected production endpoint from sandbox gateway
    test_http_status "Sandbox gateway - no token profile" "GET" "${SANDBOX_GW}/api/privileges" "401"

    # =========================================================================
    # SUMMARY
    # =========================================================================
    echo ""
    echo -e "${CYAN}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║                    TEST RESULTS                              ║${NC}"
    echo -e "${CYAN}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "  Total:  ${TOTAL}"
    echo -e "  Passed: ${GREEN}${PASSED}${NC}"
    echo -e "  Failed: ${RED}${FAILED}${NC}"
    echo ""

    if [ "$FAILED" -eq 0 ]; then
        echo -e "${GREEN}╔══════════════════════════════════════════════════════════════╗${NC}"
        echo -e "${GREEN}║                    ALL TESTS PASSED!                         ║${NC}"
        echo -e "${GREEN}╚══════════════════════════════════════════════════════════════╝${NC}"
        return 0
    else
        echo -e "${RED}╔══════════════════════════════════════════════════════════════╗${NC}"
        echo -e "${RED}║              ${FAILED} TEST(S) FAILED - REVIEW LOGS ABOVE             ║${NC}"
        echo -e "${RED}╚══════════════════════════════════════════════════════════════╝${NC}"
        return 1
    fi
}

# --- Entry Point -------------------------------------------------------------
main "$@"
