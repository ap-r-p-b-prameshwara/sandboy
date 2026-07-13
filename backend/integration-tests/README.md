# Integration Tests

End-to-end test suite for the Sandbox POC system. Validates authentication, environment switching, data isolation, and error handling across Production and Sandbox environments.

## Quick Start

```bash
# Full test run (build images, run tests, cleanup)
./backend/integration-tests.sh

# Skip docker build (use existing images)
./backend/integration-tests.sh --skip-build

# Run tests and leave services running for manual inspection
./backend/integration-tests.sh --skip-clean

# Show help
./backend/integration-tests.sh --help
```

## Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| Docker | 24+ | Container runtime |
| Docker Compose | v2+ | Orchestration |
| curl | 7+ | HTTP requests |
| jq | 1.6+ | JSON parsing |
| psql | 16+ (PG client) | Database verification |

## Architecture Under Test

```
                          ┌──────────────────┐
                          │  Auth Service     │
                          │  (Production)     │
                          └────────┬─────────┘
                                   │
  ┌──────────────┐         ┌───────┴──────────┐         ┌──────────────┐
  │  Production   │◄──────►│   API Gateway    │◄──────►│  Sandbox     │
  │  Gateway      │         │  Token Verify    │         │  Gateway     │
  │  :8080        │         └───────┬──────────┘         │  :8085       │
  └──────┬───────┘                 │                     └──────┬───────┘
         │                         │                            │
  ┌──────┴───────┐        ┌───────┴──────────┐        ┌───────┴──────┐
  │  Production   │        │                  │        │  Sandbox    │
  │  Services     │        │   Same JWT       │        │  Services   │
  │  (user, qris, │        │   Works Both     │        │  (user, qris,│
  │   cashin)     │        │   Sides          │        │   cashin)    │
  └──────┬───────┘        └──────────────────┘        └───────┬──────┘
         │                                                     │
  ┌──────┴───────┐                                    ┌───────┴──────┐
  │  Production   │                                    │  Sandbox     │
  │  PostgreSQL   │                                    │  PostgreSQL  │
  │  :5432        │                                    │  :5433       │
  └──────────────┘                                    └──────────────┘
```

## Test Scenarios

| # | Scenario | Description |
|---|----------|-------------|
| 1 | Service startup | Start all 13 backend containers, wait for health checks |
| 2 | User registration | POST /api/register → 201 with user data |
| 3 | Dual DB write | Verify user record exists in BOTH Production and Sandbox DBs |
| 4 | Login | POST /api/login → 200 with JWT token |
| 5 | Production access | GET /api/profile, /api/qris/transactions, /api/cashin/va → 200 |
| 6 | Sandbox access | Same token on :8085 → same endpoints work |
| 7 | Data isolation | Sandbox-only data does not leak to Production DB |
| 8 | Error handling | Invalid token → 401, no token → 401, wrong method → 405, empty body → 400 |

## Test Data

- Test user email includes a Unix timestamp for uniqueness: `integ-test-<timestamp>@merchant.com`
- Test data is automatically cleaned up on exit (unless `--skip-clean`)
- A separate isolated user is created/destroyed for the data leakage test

## Understanding Results

Each test step prints either `[PASS]` or `[FAIL]` with context:

```
[INFO]  Waiting for Production Gateway at http://localhost:8080/actuator/health ...
[INFO]  Production Gateway is ready (attempt 2)
[PASS]  Registration returned correct email: integ-test-1712345678@merchant.com
[PASS]  Production DB has user (found: integ-test-1712345678@merchant.com)
[PASS]  Sandbox DB has user (found: integ-test-1712345678@merchant.com)
[PASS]  Login successful - JWT token received (length: 312)
[PASS]  Production profile - correct email: integ-test-1712345678@merchant.com
[PASS]  Sandbox profile - correct email: integ-test-1712345678@merchant.com (same token works!)
[PASS]  Sandbox-only user is NOT present in Production DB (no leakage)
[PASS]  Unauthorized (no token) - Production profile (HTTP 401)
...
```

A summary is printed at the end:

```
╔══════════════════════════════════════════════════════════════╗
║                    TEST RESULTS                              ║
╚══════════════════════════════════════════════════════════════╝

  Total:  25
  Passed: 25
  Failed: 0
```

## Common Issues

| Issue | Likely Cause | Fix |
|-------|-------------|-----|
| `docker: command not found` | Docker not installed | Install Docker Desktop |
| Port already in use | Another process on 8080/8085/5432/5433 | `lsof -ti:8080 \| xargs kill` |
| `psql: FATAL: password authentication failed` | .env not sourced | The script auto-sources .env |
| Container not healthy | Service startup timeout | Increase wait time in script |
| Registration fails with 400/500 | .env JWT_SECRET not configured | Set JWT_SECRET in .env (min 256-bit) |
| Docker build fails | Java 25 mismatch | Ensure Docker has JDK 25 image |

## Extending Tests

To add a new test scenario:

1. Add a new `log_step N "Description"` block
2. Use the helper functions:
   - `test_http_status "name" "METHOD" "URL" expected_code [-H "header: value"]`
   - `test_http_body "name" "METHOD" "URL" [-H "header: value"]`
   - `check_db_record "desc" "host:port" "dbname" "query" "expected"`
3. The test count updates automatically via the `PASSED`/`FAILED` counters

## CI Integration

```yaml
# GitHub Actions example
integration-tests:
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v4
    - name: Run Integration Tests
      run: ./backend/integration-tests.sh --skip-build
```
