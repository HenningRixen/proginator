# LSP Performance Execution Checklist

## Scope
Speed up LSP readiness for interactive exercises (diagnostics + completion + hover/signature), with phased rollout and measurable acceptance criteria.

## Global Success Criteria
- [ ] Warm path: page open to `LSP ready` <= 2s.
- [ ] Cold path: page open to `LSP ready` <= 5s.
- [ ] First completion response <= 1s after `LSP ready`.
- [ ] No regression in auth checks or container isolation settings.

## Phase 0 - Baseline Instrumentation (Measure Before Changing)

### Objectives
- [x] Produce reliable timing data for backend and frontend startup path.
- [x] Establish before/after benchmark for each optimization phase.

### Code Touchpoints
- `src/main/java/com/example/prog1learnapp/controller/lsp/LspWebSocketHandler.java`
  - [x] Add timing around `afterConnectionEstablished`.
- `src/main/java/com/example/prog1learnapp/service/lsp/LspSessionManager.java`
  - [x] Add timing logs around `open`, `acquireContainer`, bridge creation/start.
- `src/main/java/com/example/prog1learnapp/service/lsp/JdtLsContainerService.java`
  - [x] Add timing logs in `acquireContainer`, `createAndStartContainer`, cleanup decisions.
- `src/main/java/com/example/prog1learnapp/service/lsp/LspBridge.java`
  - [x] Add startup/forwarding timings and first-message timing.
- `src/main/resources/static/js/exercise-lsp.js`
  - [x] Add performance marks/measurements.
  - [x] Measure WS connect start/open.
  - [x] Measure `initialize` request start/end.
  - [x] Measure `didOpen` sent.
  - [x] Measure first diagnostics receive.
  - [x] Measure first completion roundtrip.


### Automated Test Plan
- [x] Add a backend integration test that verifies LSP timing logs are emitted with correlated identifiers (`wsId`, `workspaceKey`) during connection open/close flow.
- [x] Add a backend integration test that verifies `/api/lsp/health` contract (200 vs 503, payload fields present) without depending on manual checks.
- [x] Add an automated browser benchmark via WebDriver that performs authenticated LSP handshake and captures timing metrics.
- [x] In the browser benchmark, parse and persist measured values for:
  - [x] WS open latency
  - [x] initialize latency
  - [x] first diagnostics latency
  - [x] first completion roundtrip
- [x] In the browser benchmark, run N repeated iterations (default 10) and compute median/p95 automatically.
- [x] Persist benchmark output as a CI artifact/log (`target/lsp-phase0/metrics.jsonl`, `target/lsp-phase0/summary.json`) so comparisons are scriptable across runs.
- [x] Define pass/fail thresholds for Phase 0 collection stability (at least 90% iterations should produce all four metrics).
- [x] Add a single command wrapper to run all Phase 0 automated checks locally and in CI.

### Suggested Automation Touchpoints (Phase 0)
- [x] `src/test/java/com/example/prog1learnapp/controller/lsp/LspHealthControllerIntegrationTest.java` for health contract automation.
- [x] `src/test/java/com/example/prog1learnapp/controller/lsp/LspWebSocketObservabilityIntegrationTest.java` for connection log emission/correlation checks.
- [x] `src/test/lsp-perf/` browser automation ownership/docs.
- [x] `scripts/lsp-phase0-benchmark.sh` benchmark entrypoint.
- [x] `scripts/run-phase0-automated-checks.sh` single-command phase runner.

### Exit Criteria
- [x] Baseline metrics documented.
- [x] Timing logs/marks available in dev without changing feature behavior.

### Work Breakdown - Fix `initialize` Failure in Current Environment
1. Root cause verification
- [x] Reproduce one failing benchmark run with `ENFORCE_THRESHOLD=0 ITERATIONS=1` and keep `target/lsp-phase0/app.log`.
- [x] Confirm failure signature in logs (bridge startup interruption and/or JDT LS startup traceback depending run timing).
- [x] Confirm corresponding app-side bridge failure logs (`stage=bridgeStart`, `JDT LS failed to start`).

2. Container runtime diagnosis
- [x] Add temporary diagnostic command path in LSP startup flow to log `which java`, `java -version`, and `echo $JAVA_HOME` inside the LSP container before launching JDT LS.
- [x] Verify the LSP image (`proginator-jdtls`) contains a working Java runtime executable for the `runner` user.
- [x] Verify permissions/executability of Java and JDT LS binaries inside the container.

3. Image/runtime fix
- [x] Update `src/main/docker/Dockerfile.lsp` so Java runtime path used by JDT LS is valid and executable in runtime context.
- [x] If needed, set explicit env vars in image (`JAVA_HOME`, `PATH`) compatible with the JDT LS launcher.
- [x] Rebuild image with `./build-lsp-docker-image.sh`.

4. Bridge startup hardening for diagnostics
- [x] Improve `LspBridge` startup error logging to include stderr summary when JDT LS exits early.
- [x] Ensure startup failure logs include `containerName`, `workspaceKey`, and elapsed timing.
- [x] Keep behavior non-destructive (no broad refactor yet; Phase 0 scope only).

5. Automated verification updates
- [x] Add/extend automated test to assert bridge startup failure reason is surfaced clearly in logs when backend process exits.
- [x] Run backend tests:
  - [x] `./mvnw -q -Dtest=LspHealthControllerIntegrationTest,LspWebSocketObservabilityIntegrationTest test`

6. Re-baseline after fix
- [x] Run benchmark with `ITERATIONS=10` and default threshold enforcement.
- [x] Confirm `summary.json` shows non-null values for `initializeMs`, `firstDiagnosticsMs`, `firstCompletionMs`.
- [x] Confirm success threshold passes (>= 90% complete-metric iterations).
- [x] Save updated baseline artifacts in `target/lsp-phase0/`.

7. Phase 0 closeout for this incident
- [x] Document root cause and exact fix in `docs/LSP_PHASE0_AUTOMATION_REPORT.md`.
- [x] Mark this WBS complete only after successful 10-iteration run.

---

## Phase 1 - Remove Artificial Startup Blocking (Highest Priority)

### Objectives
- [x] Eliminate unnecessary startup delay caused by current bridge startup wait behavior.

### Code Touchpoints
- `src/main/java/com/example/prog1learnapp/service/lsp/LspBridge.java`
  - [x] Refactor `waitForEarlyFailure()` so it does not block full `connectTimeoutMs` while process is alive.
  - [x] Keep fail-fast for true early process exit.
  - [x] Add short startup grace window (e.g., 300-1000ms) instead of full timeout blocking.
- `src/main/java/com/example/prog1learnapp/config/lsp/LspProperties.java`
  - [x] Optional: add dedicated property for startup grace if needed.
- `src/main/resources/application.yml`
  - [x] Optional: configure startup grace property.

### Test Cases
- [x] Unit-level behavior test: process alive after short grace => startup succeeds quickly.
- [x] Unit-level behavior test: process exits immediately => startup fails fast with meaningful error.
- [x] Integration smoke: open interactive exercise and verify LSP readiness no longer waits near 15s.
- [x] Regression: ensure bridge still closes WS on real backend failure.

### Exit Criteria
- [x] No fixed long startup wait in normal path.
- [x] Cold and warm startup significantly improved vs baseline.

---

## Phase 2 - Warm Backend Reuse Strategy (Option A Fixed)

### Objectives
- [x] Avoid repeated JDT LS process startup cost on each editor open.
- [x] Reuse warm backend where safe.
- [x] Use Option A: persist one running JDT LS process per HTTP session workspace key.

### Code Touchpoints
- `src/main/java/com/example/prog1learnapp/service/lsp/LspSessionManager.java`
  - [x] Add lifecycle management for reusable bridges/processes.
  - [x] Add mapping and reference lifecycle for WS <-> warm backend binding.
- `src/main/java/com/example/prog1learnapp/service/lsp/LspBridge.java`
  - [x] Support reuse semantics (attach/detach vs always spawn/kill).
- `src/main/java/com/example/prog1learnapp/service/lsp/JdtLsContainerService.java`
  - [x] Align container ref-count/TTL with Option A reuse strategy.
  - [x] Ensure cleanup remains safe for idle resources.
- `src/main/java/com/example/prog1learnapp/controller/lsp/LspWebSocketHandler.java`
  - [x] Keep connection/cleanup logic consistent with reused backend sessions.

### Test Cases
- [x] Reopen test: open exercise, close tab, reopen same exercise/session => second load faster than first.
- [x] Concurrent WS test: multiple WS sessions for same authenticated user/session do not corrupt bridge state.
- [x] Cleanup test: idle backend removed after TTL when no references.
- [x] Resource cap test: `max-sessions` still enforced.

### Exit Criteria
- [x] Warm reopen path meets <= 2s target.
- [x] No leaked bridges/containers across repeated connects/disconnects.

---

## Phase 3 - Workspace and Index Warmup

### Objectives
- [ ] Reduce time until useful diagnostics/completions after connection.

### Code Touchpoints
- `src/main/java/com/example/prog1learnapp/controller/LearnController.java`
  - [x] Revisit `lspWorkspaceUri` derivation consistency with backend workspace keys.
- `src/main/resources/static/js/exercise-lsp.js`
  - [x] Ensure bootstrap sequence is minimal and deterministic.
  - [x] Use sequence: initialize -> initialized -> didOpen -> debounced didChange.
  - [x] Send latest document state before completion/hover/signature requests.
- `src/main/java/com/example/prog1learnapp/service/lsp/LspSessionManager.java`
  - [x] Optional: expose stable workspace mapping metadata if client/server alignment is improved.

### Test Cases
- [x] First-diagnostics latency test: first `publishDiagnostics` arrives faster vs previous phase.
- [x] Completion readiness test: trigger completion immediately after editor open; success rate and latency improve.
- [x] Workspace continuity test: reopen same session and confirm faster semantic readiness (cache reuse effect).

### Exit Criteria
- [ ] Measurable drop in first diagnostics and first completion latency.
- [ ] No URI/workspace mismatches causing dropped diagnostics.

---

## Phase 4 - Client Responsiveness and UX Signaling

### Objectives
- [x] Improve perceived speed and avoid silent waiting.

### Code Touchpoints
- `src/main/resources/static/js/exercise-lsp.js`
  - [x] Add explicit LSP state machine (`connecting`, `ready`, `degraded`, `failed`).
  - [x] Gate request-heavy providers until `ready`.
  - [x] Add retry/backoff for transient request failures.
- `src/main/resources/templates/exercise.html`
  - [x] Add a small non-intrusive UI indicator for LSP status.
- `src/main/resources/static/css/exercise.css`
  - [x] Add styles for LSP status indicator states.

### Test Cases
- [x] UX state test: indicator transitions correctly on normal start, socket error, backend unavailable.
- [x] Degraded mode test: editor remains usable without LSP; no blocking JS errors.
- [x] Request-throttle test: no burst of completion requests during initialization phase.

### Exit Criteria
- [x] Users see clear LSP readiness state.
- [x] No dead/blocked autocomplete attempts during startup.

---

## Phase 5 - Capacity and Config Tuning

### Objectives
- [x] Tune runtime limits based on measured usage.

### Code Touchpoints
- `src/main/resources/application.yml`
  - [x] Tune `app.lsp.max-sessions`.
  - [x] Tune `app.lsp.memory-mb`.
  - [x] Tune `app.lsp.cpus`.
  - [x] Tune `app.lsp.idle-ttl-seconds`.
  - [x] Tune `app.lsp.cleanup-interval-ms`.
- `src/main/java/com/example/prog1learnapp/service/lsp/JdtLsContainerService.java`
  - [x] Add clearer saturation logs/metrics for session-limit hits.

### Test Cases
- [x] Load test (dev/staging): simulate N concurrent interactive users.
- [x] Verify container count, WS stability, median/p95 latencies under load.
- [x] Saturation behavior: when `max-sessions` reached, failure is explicit and safe.
- [x] Cleanup behavior: no premature cleanup of active resources.

### Exit Criteria
- [x] Stable performance under expected concurrent load.
- [x] Predictable behavior at resource limits.

---

## Security and Regression Checklist (Run Each Phase)
- [x] Auth still required for `/api/lsp/ws` and `/api/lsp/health`.
- [x] Container hardening flag `--network=none` remains unchanged.
- [x] Container hardening flag `--read-only` remains unchanged.
- [x] Container hardening flag `--cap-drop=ALL` remains unchanged.
- [x] Container hardening flag `--security-opt=no-new-privileges` remains unchanged.
- [x] Container runs as non-root `runner`.
- [x] No changes that expose execution/LSP endpoints publicly.
- [x] No regressions in interactive editor fallback behavior when LSP unavailable.

## Suggested Test File Additions
- [x] `src/test/java/com/example/prog1learnapp/service/lsp/LspBridgeStartupTest.java`
- [x] `src/test/java/com/example/prog1learnapp/service/lsp/LspSessionManagerTest.java`
- [x] `src/test/java/com/example/prog1learnapp/controller/lsp/LspHealthControllerIntegrationTest.java`
- [ ] `src/test/java/com/example/prog1learnapp/controller/lsp/LspWebSocketHandlerIntegrationTest.java`

---

## Phase 6 - Prewarm on Login (Planned + Implementation)

### Phase A - Config + Wiring
- [x] Add `app.lsp.prewarm-on-login` flag in config/properties (default `false`).
- [x] Keep behavior fully no-op when flag is disabled.

### Phase B - Background Prewarm Trigger
- [x] Trigger prewarm asynchronously after successful login (non-blocking).
- [x] Use existing workspace key contract (`principal:httpSessionId`).
- [x] Reuse existing LSP bridge/container lifecycle (no duplicate stack).
- [x] Skip prewarm when backend for workspace already exists.

### Phase C - Safety Controls
- [x] Limit to one prewarm attempt in-flight per session key.
- [x] Respect `max-sessions` saturation path (safe skip + log).
- [x] Enforce prewarm timeout guard.
- [x] Add structured prewarm logs (`start|skip|success|fail`, duration, workspaceKey).

### Phase D - Measurement
- [x] Add/keep login timing marker to compare impact.
- [x] Keep exercise first `initialize` / first diagnostics metrics collection.
- [x] Use saturation counters (`saturationRejectCount`) as acceptance signal.
- [x] Add an automated before/after compare runner for prewarm `off` vs `on`.

### Phase E - Tests
- [x] Prewarm disabled => no prewarm call.
- [x] Prewarm enabled => async prewarm trigger executes once per session key.
- [x] Existing backend => prewarm skipped.
- [x] Saturation/failure path => safe skip/fail with no login break.
- [x] Login remains successful even if prewarm fails.

### Exit Criteria
- [x] Login flow remains non-blocking.
- [ ] Exercise first `initialize` / diagnostics improves in measured runs.
- [x] Saturation rejects remain explicit and controlled.
- [x] Feature can be disabled instantly via `app.lsp.prewarm-on-login=false`.
