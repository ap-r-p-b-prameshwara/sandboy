# Sandbox PoC — Environment Isolation Architecture

Single shell application with environment switching between Production and Sandbox modes, featuring complete data isolation via clone architecture.

## Architecture Overview

```
                            +-------------------+
                            |     BROWSER       |
                            |  localhost:4200   |
                            |  (single shell)   |
                            +---------+---------+
                                      |
              +-----------------------+-----------------------+
              |                       |                       |
        [Register/Login]       [Sidebar Click]         [Env Switch]
              |                       |                       |
              v                       v                       v
      +-------+-------+       +-------+-------+       +-------+
      |  gateway-prod  |       |   iframe src   |       |  localStorage
      |   :8080        |       |  ?token=xxx    |       | appEnvironment
      +-------+-------+       |  :4201/:4202   |       +-------+
              |               |  :4204/:4205   |               |
              |               +-------+-------+               v
              |                       |               [EnvironmentService]
              |                       v               sets apiUrl :8080/:8085
              |               +-------+-------+
              |               |  Microfrontend  |
              |               |  (iframe)       |
              |               |  reads token    |
              |               |  from URL       |
              |               |  sets Bearer    |
              |               |  in header      |
              |               +-------+-------+
              |                       |
              v                       v
      +-------+-------+       +-------+-------+
      | TokenVerif-    |       |  Gateway      |
      | icationFilter  |       |  :8080/:8085  |
      | check env claim|       |  verifikasi   |
      | inject X-User-Id|      |  validasi env |
      +-------+-------+       +-------+-------+
              |                       |
              v                       v
     +--------+--------+     +--------+--------+
     |   PRODUCTION    |     |    SANDBOX      |
     |   :8080         |     |   :8085         |
     +--------+--------+     +--------+--------+
              |                       |
    +---------+---------+   +---------+---------+
    |                   |   |                   |
    v                   v   v                   v
+-----------+ +-----------+ +-----------+ +-----------+
|auth-serv. | |user-serv. | |auth-serv. | |user-serv. |
|:8083      | |:8081      | |:8084      | |:8082      |
+-----------+ +-----------+ +-----------+ +-----------+
    |                   |       |               |
    v                   v       v               v
+-----------+ +-----------+ +-----------+ +-----------+
|qris-serv. | |cashin-serv| |qris-serv. | |cashin-serv|
|:8086      | |:8088      | |:8087      | |:8089      |
+-----------+ +-----------+ +-----------+ +-----------+
    |                   |       |               |
    v                   v       v               v
+-----------+ +-----------+ +-----------+ +-----------+
|Postgres   | |  Redis    | |Postgres   | |  Redis    |
|:5432      | |:6379      | |:5433      | |:6379      |
+-----------+ +-----------+ +-----------+ +-----------+
```

## Tech Stack

- **Backend**: Spring Boot 4.0 + Java 25
- **Frontend**: Angular 20 (standalone components + Angular Material)
- **Database**: PostgreSQL 16 (separate instances per environment)
- **Cache**: Redis 7
- **Container**: Docker Compose (13 containers)
- **Authentication**: JWT (HMAC-SHA384)
- **API Gateway**: Spring Cloud Gateway 5.0 (WebFlux)

## Services

### Frontend (5 apps)

| App | Port | Type | Gateway |
|-----|------|------|---------|
| main-web | 4200 | Shell (single page) | :8080 / :8085 |
| qris-web | 4201 | Microfrontend QRIS (prod) | :8080 |
| cashin-web | 4202 | Microfrontend CashIn (prod) | :8080 |
| qris-web-sandbox | 4204 | Microfrontend QRIS (sandbox) | :8085 |
| cashin-web-sandbox | 4205 | Microfrontend CashIn (sandbox) | :8085 |

### Backend (13 containers)

| Container | Port | Description |
|-----------|------|-------------|
| postgres-prod | 5432 | Database Production |
| postgres-sandbox | 5433 | Database Sandbox |
| redis | 6379 | Shared Redis (session/token cache) |
| user-service-prod | 8081 | User registration, profile, dual-write |
| user-service-sandbox | 8082 | User service (sandbox DB) |
| auth-service-prod | 8083 | Login, JWT generation, token verification |
| auth-service-sandbox | 8084 | Sandbox token generation (no password) |
| gateway-prod | 8080 | API Gateway Production |
| gateway-sandbox | 8085 | API Gateway Sandbox |
| qris-service-prod | 8086 | QRIS (real integration) |
| qris-service-sandbox | 8087 | QRIS (dummy mode) |
| cashin-service-prod | 8088 | CashIn (real) |
| cashin-service-sandbox | 8089 | CashIn (clone) |

## Authentication Flow

### Register
```
FE -> gateway-prod(:8080) -> user-service-prod(:8081)
  -> Insert ke DB prod + DB sandbox (dual-write)
  -> Insert credential (prod + sandbox)
  -> Privilege prod: [CASH_IN, DASHBOARD]
  -> Privilege sandbox: [QRIS, CASH_IN, DASHBOARD] + merchant QRIS auto
```

### Login (Production only)
```
FE -> gateway-prod(:8080) -> auth-service-prod(:8083)
  -> Validasi password
  -> Generate JWT { env: "production", userId, email }
  -> Simpan di Redis
  -> Return token
```

### Switch to Sandbox
```
1. Cek: sandboxToken di localStorage?
   - ADA  -> verify -> OK -> tampil dashboard sandbox
   - TIDAK -> requestSandboxToken()
2. FE -> gateway-sandbox(:8085) -> auth-service-sandbox(:8084)
3. Auth-sandbox cari user di DB sandbox:
   - ADA  -> generate JWT { env: "sandbox" } -> return
   - TIDAK -> SYNC dari production
4. Simpan sandboxToken di localStorage
```

## Key Design Decisions

| Aspect | Decision |
|--------|----------|
| **Data Isolation** | Separate PostgreSQL instances (5432 vs 5433) — no shared tables |
| **Clone Architecture** | Same codebase for prod/sandbox services, different config profiles |
| **Token Isolation** | JWT contains `env` claim — gateway validates match |
| **Sandbox Sync** | On-demand sync from production on first sandbox login (no background job) |
| **Frontend** | Single shell app with environment switcher in-app |
| **QRIS Sandbox** | Dummy mode — generates fake QRIS codes, no real integration |

## Getting Started

### Prerequisites
- Java 25 (via sdkman: `sdk use java 25.0.3-tem`)
- Docker Desktop
- Node.js 20+

### Start Backend
```bash
source ~/.sdkman/bin/sdkman-init.sh && sdk use java 25.0.3-tem
cd backend
docker compose --profile backend up -d --build
```

### Start Frontend
```bash
cd frontend/main-web && npm install && npx ng serve --host 0.0.0.0 --port 4200
cd frontend/qris-web && npm install && npx ng serve --host 0.0.0.0 --port 4201
cd frontend/cashin-web && npm install && npx ng serve --host 0.0.0.0 --port 4202
cd frontend/qris-web-sandbox && npm install && npx ng serve --host 0.0.0.0 --port 4204
cd frontend/cashin-web-sandbox && npm install && npx ng serve --host 0.0.0.0 --port 4205
```

### Access
- **main-web**: http://localhost:4200

## Pros & Cons

| Aspect | Pro | Con |
|--------|-----|------|
| **Data Isolation** | ✅ DB benar-benar terpisah (5432 vs 5433) | ❌ Dual-write nambah latency |
| **Clone Architecture** | ✅ Kode identik, predictable | ❌ 2x resource |
| **Token Security** | ✅ JWT env claim dicek gateway | ❌ Sandbox skip password (by design) |
| **On-Demand Sync** | ✅ Zero overhead | ❌ Gagal sync = gagal sandbox |
| **Gateway Isolation** | ✅ Beda port, beda env validation | ❌ Codebase sama |
| **Frontend** | ✅ 1 shell, switch in-app | ❌ Iframe, bukan Module Federation |
| **Auth Service** | ✅ auth-sandbox mandiri | ❌ Tetep verify ke auth-prod |
