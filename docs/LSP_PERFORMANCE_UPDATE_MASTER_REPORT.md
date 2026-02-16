# LSP Performance Update - Master Report

## Scope
This document consolidates all LSP performance work completed in this repository across:
- Phase 0: baseline instrumentation and automation
- Phase 1: startup wait removal
- Phase 2: warm backend reuse (Option A)
- Phase 3: workspace/URI alignment and deterministic bootstrap
- Phase 4: client responsiveness and UX signaling
- Phase 5: capacity tuning and saturation observability
- Phase 5.1: multi-session saturation validation tooling
- Phase 6: optional login prewarm and post-phase stability hardening

Primary checklist source:
- `LSP_PERFORMANCE_EXECUTION_CHECKLIST.md`

Supporting reports:
- `docs/LSP_PHASE0_AUTOMATION_REPORT.md`
- `docs/LSP_PHASE1_IMPLEMENTATION_REPORT.md`
- `docs/LSP_PHASE2_IMPLEMENTATION_REPORT.md`
- `docs/LSP_PHASE3_IMPLEMENTATION_REPORT.md`
- `docs/LSP_PHASE4_IMPLEMENTATION_REPORT.md`
- `docs/LSP_PHASE5_IMPLEMENTATION_REPORT.md`
- `docs/LSP_TEST_FILE_ADDITIONS_STATUS.md`

---

## Final Status Overview

### Checklist status (current)
- Phase 0: complete
- Phase 1: complete
- Phase 2: complete
- Phase 3: implementation/test touchpoints complete; objective/exit metrics still not conclusively improved vs Phase 2 medians
- Phase 4: complete
- Phase 5: complete
- Security/regression checklist: complete
- Suggested test additions: first 3 completed; `LspWebSocketHandlerIntegrationTest` intentionally not added

### Global success criteria status
Still open in checklist:
- warm path <= 2s end-to-end ready
- cold path <= 5s end-to-end ready
- first completion <= 1s after ready

Observed data indicates warm-path strengths but cold/multi-session initialize remains costly.

### Post-Phase 6 stability status (2026-02-16)
- Resolved a backend lifecycle bug where a dead prewarmed bridge could be reused.
- Added stale bridge eviction/recreate behavior in `LspSessionManager`.
- Improved WS close classification for backend outage vs client bad payload.
- Increased default LSP container memory from `384` to `512` MB.

---

## Architecture and Behavioral Changes

### Backend lifecycle and reuse
1. Removed artificial startup blocking in bridge startup grace path.
2. Introduced workspace-scoped bridge/process reuse (Option A):
   - one warm JDT LS backend per HTTP session workspace key
   - attach/detach WebSocket semantics instead of spawn/kill per WS.
3. Added shared workspace naming contract:
   - `LspWorkspaceNaming.workspaceKey(...)`
   - `LspWorkspaceNaming.workspaceUri(...)`
   - consistent URI derivation between controller and backend.
4. Added bridge liveness checks and stale bridge eviction:
   - dead prewarmed bridge is removed and recreated on next open/forward/health-prewarm path.

### Frontend protocol and resilience
1. Deterministic LSP bootstrap sequence:
   - `initialize -> initialized -> didOpen -> ready`
2. Latest-document correctness:
   - unsynced edits tracked and flushed before request-heavy providers.
3. Explicit client state machine:
   - `connecting`, `ready`, `degraded`, `failed`
4. Provider readiness gating:
   - completion/hover/signature wait until ready and fail safely.
5. Retry/backoff for transient request failures.
6. Completion burst guard:
   - in-flight completion request sharing to avoid startup bursts.
7. UX indicator added to exercise page for visible LSP state.

### Capacity and observability
1. Tuned LSP runtime settings in `application.yml`:
   - `max-sessions=20`
   - `memory-mb=512`
   - `cpus=1.5`
   - `idle-ttl-seconds=180`
   - `cleanup-interval-ms=15000`
2. Added saturation counters in `JdtLsContainerService`:
   - acquire attempts/reuse/create/failures
   - saturation reject count
   - last saturated key/timestamp
3. Exposed saturation snapshot via `/api/lsp/health`.

---

## Files Added / Updated (Core)

### New key files
- `src/main/java/com/example/prog1learnapp/service/lsp/LspWorkspaceNaming.java`
- `scripts/lsp-phase4-ux-check.sh`
- `scripts/lsp-phase5-load-check.sh`
- `scripts/lsp-phase5.1-multisession-check.sh`
- `src/test/java/com/example/prog1learnapp/service/lsp/LspWorkspaceNamingTest.java`
- `src/test/java/com/example/prog1learnapp/service/lsp/JdtLsContainerServiceSaturationTest.java`

### Notable updated files
- `src/main/resources/static/js/exercise-lsp.js`
- `src/main/resources/templates/exercise.html`
- `src/main/resources/static/css/exercise.css`
- `src/main/java/com/example/prog1learnapp/controller/LearnController.java`
- `src/main/java/com/example/prog1learnapp/service/lsp/LspSessionManager.java`
- `src/main/java/com/example/prog1learnapp/service/lsp/LspBridge.java`
- `src/main/java/com/example/prog1learnapp/service/lsp/JdtLsContainerService.java`
- `src/main/java/com/example/prog1learnapp/controller/lsp/LspHealthController.java`
- `src/main/java/com/example/prog1learnapp/controller/lsp/LspWebSocketHandler.java`
- `src/main/resources/application.yml`
- `scripts/lsp-phase0-benchmark.sh`

---

## Benchmark and Validation Summary

## Phase 0 baseline/automation
- Browser benchmark automation and summary artifacts implemented.
- 10-iteration run achieved complete metric capture and threshold pass.

## Phase 1 result
- Removed full-timeout startup wait; fail-fast retained.
- Startup path no longer blocked by artificial wait.

## Phase 2 result
- Warm reuse delivered strong reopen path.
- Example reported median values:
  - initialize: ~single-digit ms on warm iterations
  - diagnostics/completion: low hundreds ms on warm iterations

## Phase 3 host benchmark (provided)
`ITERATIONS=10` summary:
- `wsOpenMs`: median `7.5`, p95 `65`
- `initializeMs`: median `7.0`, p95 `16207`
- `firstDiagnosticsMs`: median `150.5`, p95 `19293`
- `firstCompletionMs`: median `131.5`, p95 `19568`

Interpretation:
- reliability and URI/workspace consistency improved.
- warm behavior good, but median latency improvement vs prior phase not clearly established.

## Phase 4 host UX validation (provided)
`scripts/lsp-phase4-ux-check.sh` passed:
- normal transition reached `ready`
- forced socket close transitioned to `degraded`
- unavailable backend scenario transitioned to `failed`
- health endpoint remained healthy in normal path

## Phase 5 host load validation (provided)
`scripts/lsp-phase5-load-check.sh` with `USERS=8`:
- success rate `1.0`
- health `200`
- no saturation rejects
- due to Phase 2 session reuse this was single-session backend pressure, not multi-session saturation.

## Phase 5.1 multi-session runs (provided)
1. Safe laptop baseline (`SESSIONS=8`, staggered):
- `okCount=8`, `failCount=0`, `successRate=1.0`, health `200`
- active containers/bridges both `8`
- no saturation rejects
- initialize latency still high under cold multi-session startup bursts.

2. Non-saturated baseline (`SESSIONS=10`):
- `okCount=9`, `failCount=1`, `successRate=0.9`
- no saturation rejects.

3. Saturated runs (`SESSIONS=24`, maxSessions=20):
- saturation reject counters increased (`saturationRejectCount=7`)
- active containers/bridges capped at 20
- backend remained responsive (`health=200`)
- success quality under saturation still poor (very low okCount), indicating overload/startup contention.

## Post-Phase 6 backend stability fix (implemented)

### Symptom
- user-visible sequence: login prewarm succeeds, later interactive WS emits
  - `LSP message forwarding failed: LSP backend process is not running`

### Root cause
- `LspSessionManager.open(...)` reused existing workspace bridge records without verifying process liveness.
- if prewarmed JDT LS process died later, stale bridge remained mapped and failed on first forwarded request.

### Fixes implemented
1. Added bridge liveness API
- `LspBridge.isRunning()` to gate reuse and forwarding.

2. Added stale bridge eviction/recreate path
- `LspSessionManager` now evicts stale records and force-removes mapped containers when a dead bridge is detected in:
  - `open(...)` reuse path
  - `forwardClientMessage(...)`
  - `hasBridgeForWorkspaceKey(...)`
  - `prewarmWorkspace(...)`

3. Improved WS error mapping
- `LspWebSocketHandler.handleTextMessage(...)` now returns:
  - `BAD_DATA` for oversized payload errors
  - `SERVER_ERROR` for backend unavailability/errors

4. Raised default memory headroom
- `app.lsp.memory-mb` changed to `512`.

---

## Test Coverage Summary

### Implemented and validated
- `LspBridgeStartupTest`
- `LspSessionManagerTest`
- `LspHealthControllerIntegrationTest`
- `LspWebSocketObservabilityIntegrationTest`
- `LspWorkspaceNamingTest`
- `JdtLsContainerServiceSaturationTest`
- `LspPrewarmServiceTest`
- `LspLoginSuccessHandlerTest`

### Suggested but intentionally not added
- `LspWebSocketHandlerIntegrationTest`

### Additional regression tests added for stability fix
- `LspSessionManagerTest.open_whenPrewarmedBridgeDied_recreatesBridgeAndContainer`
- `LspWebSocketObservabilityIntegrationTest.handleTextMessage_closesWithServerErrorWhenBackendUnavailable`
- `LspWebSocketObservabilityIntegrationTest.handleTextMessage_closesWithBadDataWhenPayloadTooLarge`

Reference:
- `docs/LSP_TEST_FILE_ADDITIONS_STATUS.md`

---

## Security and Regression Verification

Checklist items are marked complete based on code verification:
- auth required for `/api/lsp/ws` and `/api/lsp/health`
- container hardening unchanged:
  - `--network=none`
  - `--read-only`
  - `--cap-drop=ALL`
  - `--security-opt=no-new-privileges`
  - `--user=runner`
- no new public exposure of LSP/execution endpoints
- interactive editor fallback remains available when LSP fails

---

## Key Findings

1. Biggest win
- Warm reuse (Phase 2) is the dominant performance improvement.

2. Biggest remaining bottleneck
- Cold and multi-session initialize latency remains high during concurrent starts.

3. Saturation behavior
- Limit enforcement and counters are now visible and safe (reject path observable), but user-level success under high oversubscription is still weak.

4. Tooling maturity
- Automation now covers:
  - baseline perf metrics
  - UX state transitions
  - load and saturation telemetry
  - security/regression guardrails.

---

## Operational Guidance

1. Developer laptop runs
- Use safe baseline ranges:
  - `SESSIONS=8..12`
  - `STAGGER_MS>=500`
  - shorter `HOLD_MS` to reduce pressure

2. CI/staging runs
- Run true saturation (`SESSIONS > max-sessions`) only on stronger infrastructure.

3. Evaluate Phase 3 global criteria with consistent methodology
- compare warm-only and cold-first separately
- avoid mixing first cold outlier with warm steady-state decisions.

---

## Recommended Next Work

1. Improve overloaded startup behavior
- add admission/backpressure strategy for new LSP session opens near saturation.

2. Improve cold-start latency
- investigate container/image warmup and JDT LS readiness optimizations beyond current reuse model.

3. Tighten saturation acceptance policy further
- define explicit minimum successful admissions under overloaded scenarios (capacity-aligned SLO).

4. Add optional dedicated `LspWebSocketHandlerIntegrationTest` if stricter WS lifecycle regression coverage is needed.
