# LSP Phase 5 Implementation Report

## Scope
Phase 5 focused on LSP capacity tuning and observability at load/resource limits.

Reference docs used:
- `docs/LSP_FEATURE.md`
- `docs/LSP_PHASE4_IMPLEMENTATION_REPORT.md`
- `LSP_PERFORMANCE_EXECUTION_CHECKLIST.md`

## Changes Implemented

### 1. Runtime config tuning
Updated `src/main/resources/application.yml` (`app.lsp`):
- `max-sessions`: `50` -> `20`
- `memory-mb`: `512` -> `384`
- `cpus`: `1.0` -> `1.5`
- `idle-ttl-seconds`: `300` -> `180`
- `cleanup-interval-ms`: `30000` -> `15000`

Intent:
- reduce idle resource retention time
- increase per-container CPU share for responsiveness
- cap concurrent sessions more conservatively to avoid host overload

### 2. Saturation metrics and clearer limit-hit logs
Updated `src/main/java/com/example/prog1learnapp/service/lsp/JdtLsContainerService.java`:
- Added tracked counters:
  - `acquireAttempts`
  - `acquireReuseCount`
  - `acquireCreateCount`
  - `acquireFailureCount`
  - `saturationRejectCount`
  - `lastSaturationEpochMs`
  - `lastSaturationSessionKey`
- Added explicit saturation log on max-session rejection:
  - `stage=saturation`
  - includes `sessionKey`, `activeSessions`, `maxSessions`, and counters
- Added `getSaturationSnapshot()` for external monitoring/health visibility.

### 3. Health endpoint exposes saturation telemetry
Updated `src/main/java/com/example/prog1learnapp/controller/lsp/LspHealthController.java`:
- `/api/lsp/health` payload now includes nested `saturation` object with current counters and last saturation metadata.

### 4. Phase 5 load and capacity test automation
Added `scripts/lsp-phase5-load-check.sh`:
- boots app + chromedriver
- authenticates dev user
- opens interactive exercise
- simulates concurrent LSP websocket clients (`USERS`, default 8)
- collects:
  - success rate
  - ws/open + initialize + completion median/p95
  - `/api/lsp/health` container/bridge/saturation snapshot
- writes:
  - `target/lsp-phase5/load-raw.json`
  - `target/lsp-phase5/load-summary.json`

### 5. Deterministic service-level tests for saturation and cleanup safety
Added `src/test/java/com/example/prog1learnapp/service/lsp/JdtLsContainerServiceSaturationTest.java`:
- verifies explicit safe rejection at max sessions and saturation counters increment
- verifies cleanup does not remove active sessions (`refCount > 0`)

Updated `src/test/java/com/example/prog1learnapp/controller/lsp/LspHealthControllerIntegrationTest.java`:
- verifies `saturation` fields are present and mapped in health payload (200 and 503 paths).

## Checklist Updates
Updated `LSP_PERFORMANCE_EXECUTION_CHECKLIST.md`:
- Phase 5 objectives, code touchpoints, test cases, and exit criteria marked complete.

## Validation

### Backend tests run
```bash
./mvnw -q -Dtest=JdtLsContainerServiceSaturationTest,LspHealthControllerIntegrationTest,LspSessionManagerTest,LspBridgeStartupTest,LspWebSocketObservabilityIntegrationTest test
```
Result: passed.

### Phase 5 load script run in this sandbox
Attempted:
```bash
USERS=8 MAVEN_REPO_LOCAL=/home/hr/.m2/repository ./scripts/lsp-phase5-load-check.sh
```
In this sandbox, local port bind remains restricted, so app bootstrap timed out waiting for:
- `http://127.0.0.1:18080/login`

### Host/CI run command
```bash
chmod +x scripts/lsp-phase5-load-check.sh
USERS=8 MAVEN_REPO_LOCAL="$HOME/.m2/repository" ./scripts/lsp-phase5-load-check.sh
cat target/lsp-phase5/load-summary.json
```

### Host run result (provided)
Observed summary:
- `usersRequested`: `8`
- `usersCompleted`: `8`
- `okCount`: `8`
- `successRate`: `1.0`
- `thresholdPass`: `true`
- `wsOpenMs`: median `65`, p95 `129`
- `initializeMs`: median `222.5`, p95 `277`
- `completionMs`: median `2019.5`, p95 `2022`
- health: `200`
- saturation:
  - `activeContainers=1`
  - `activeBridges=1`
  - `saturationRejectCount=0`
  - `maxSessions=20`

Interpretation:
- stability under concurrent request pressure is good (all clients succeeded).
- this run exercised **single-session backend reuse** (Phase 2 Option A), not true multi-session saturation, because all virtual users were created in one authenticated browser session.

## Why Phase 5.1 is necessary
The current load check validates throughput and stability for one warm backend per HTTP session. It does not validate max-session enforcement behavior under many distinct HTTP sessions.

Without multi-session testing, these risks remain partially unverified:
- session-cap saturation path (`max-sessions`) under real concurrent users
- predictable reject behavior when capacity is exhausted
- cleanup dynamics when many distinct sessions churn

## Phase 5.1 Execution Checklist (Multi-Session Saturation)

- [x] Implemented dedicated multi-session load harness:
  - `scripts/lsp-phase5.1-multisession-check.sh`
- [x] Harness creates **distinct authenticated WebDriver sessions** (`SESSIONS`) and drives each to an interactive exercise.
- [x] Harness launches concurrent websocket/LSP initialize flows across all sessions and captures per-session result files.
- [x] Added saturation scenario support (`SESSIONS > max-sessions`) and summary gating using health saturation counters.
- [x] Added cleanup scenario support:
  - waits post-run window and captures second health snapshot (`cleanupHealth`).
- [x] Added aggregated summary artifact:
  - `target/lsp-phase5.1/summary.json`
  - includes success rate, median/p95 metrics, saturation counters, and cleanup snapshot.
- [x] Added threshold logic:
  - non-saturated target: success-rate >= 90%
  - saturated target: explicit rejects + non-crashing healthy endpoint response.

### Phase 5.1 host/CI command
```bash
chmod +x scripts/lsp-phase5.1-multisession-check.sh
SESSIONS=24 MAVEN_REPO_LOCAL="$HOME/.m2/repository" ./scripts/lsp-phase5.1-multisession-check.sh
cat target/lsp-phase5.1/summary.json
```

### Phase 5.1 host findings

#### Run 0: safe laptop baseline (`SESSIONS=8`, `STAGGER_MS=750`, `HOLD_MS=4000`)
- `sessionsCompleted`: `8`
- `okCount`: `8`
- `failCount`: `0`
- `successRate`: `1.0`
- `thresholdPass`: `true`
- metrics:
  - `wsOpenMs`: median `57.0`, p95 `143`
  - `initializeMs`: median `15104.0`, p95 `38534`
- health:
  - `status=200`
  - `activeContainers=8`
  - `activeBridges=8`
  - `saturationRejectCount=0`
- interpretation:
  - multi-session baseline stability is good at safe laptop load
  - saturation path is intentionally not exercised at this level
  - high initialize latency indicates cold-start pressure remains significant under multi-session bursts

#### Run A: non-saturated baseline (`SESSIONS=10`)
- `sessionsCompleted`: `10`
- `okCount`: `9`
- `failCount`: `1`
- `successRate`: `0.9`
- health:
  - `activeContainers=10`
  - `activeBridges=10`
  - `saturationRejectCount=0`
- interpretation:
  - multi-session path is working (distinct sessions create distinct backends)
  - baseline stability meets target at this level

#### Run B: saturated scenario (`SESSIONS=24`, `maxSessions=20`)
- `sessionsCompleted`: `24`
- `okCount`: `0`
- `failCount`: `24`
- health:
  - `activeContainers=20`
  - `activeBridges=20`
  - `saturationRejectCount=7`
  - `status=200` (backend still responsive)
- interpretation:
  - saturation is being hit and recorded
  - but acceptance outcome is not good yet, because no session completed successfully in this run

### Phase 5.1 acceptance correction
Initial saturated-pass logic was too permissive (`rejects > 0` + failures + healthy endpoint).

Updated `scripts/lsp-phase5.1-multisession-check.sh` threshold for saturated runs:
- require capacity-aligned outcome:
  - `okCount >= maxSessions - 2`
  - `failCount >= sessionsRequested - maxSessions`
  - `saturationRejectCount >= sessionsRequested - maxSessions`
  - health endpoint remains `200`

Additionally added launch staggering in the script:
- new env var: `STAGGER_MS` (default `200`)
- sessions are started with small spacing to reduce artificial timeout storms and produce clearer saturation/reject signals.

This prevents heavily degraded saturated runs (for example `okCount=1`, `failCount=23`) from being treated as pass.

## Operational recommendation
- Use safe baseline runs on developer laptops (`SESSIONS=8..12`, with `STAGGER_MS>=500`).
- Run true saturation validation (`SESSIONS > max-sessions`) only on CI/staging infrastructure with sufficient CPU/RAM headroom.
