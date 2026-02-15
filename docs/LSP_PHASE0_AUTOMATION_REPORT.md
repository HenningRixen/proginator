# LSP Phase 0 Automation Report

## Purpose
This document records what was implemented for **Phase 0 (Baseline Instrumentation + Automated Validation)** of LSP performance work.

## Implemented Changes

### 1. Backend observability instrumentation
Added timing/correlation logs for LSP startup path:
- `src/main/java/com/example/prog1learnapp/controller/lsp/LspWebSocketHandler.java`
- `src/main/java/com/example/prog1learnapp/service/lsp/LspSessionManager.java`
- `src/main/java/com/example/prog1learnapp/service/lsp/JdtLsContainerService.java`
- `src/main/java/com/example/prog1learnapp/service/lsp/LspBridge.java`

What is logged now:
- websocket connect duration (`wsId`)
- session open/acquire/start durations (`wsId`, `workspaceKey`)
- container acquire/create/start/cleanup timings
- first server message timing from bridge

### 2. Frontend performance instrumentation
Enhanced LSP client metrics collection in:
- `src/main/resources/static/js/exercise-lsp.js`

Added measurements:
- WS connect start/open
- initialize start/end
- didOpen sent
- first diagnostics latency
- first completion roundtrip

Also added a test-readable event sink:
- `window.__lspPerfEvents`

### 3. Backend automated tests
Added health contract integration tests:
- `src/test/java/com/example/prog1learnapp/controller/lsp/LspHealthControllerIntegrationTest.java`

Added observability integration tests:
- `src/test/java/com/example/prog1learnapp/controller/lsp/LspWebSocketObservabilityIntegrationTest.java`

Validated run:
```bash
./mvnw -q -Dtest=LspHealthControllerIntegrationTest,LspWebSocketObservabilityIntegrationTest test
```

### 4. Browser benchmark automation
Added repeatable browser benchmark script:
- `scripts/lsp-phase0-benchmark.sh`

What it does:
- starts app on configurable port
- starts `chromedriver`
- logs in with dev user (`test` / `test123`)
- runs configurable N iterations (default 10)
- captures LSP handshake/perf metrics
- computes median/p95 and threshold pass/fail
- writes artifacts to:
  - `target/lsp-phase0/metrics.jsonl`
  - `target/lsp-phase0/summary.json`

Threshold behavior:
- default: enforce >= 90% complete-metric iterations
- override: `ENFORCE_THRESHOLD=0` for non-blocking diagnostic runs

### 5. One-command Phase 0 runner
Added combined runner:
- `scripts/run-phase0-automated-checks.sh`

It runs:
1. backend Phase 0 tests
2. browser benchmark

### 6. Test harness folder documentation
Added:
- `src/test/lsp-perf/README.md`

## Commands

Run all Phase 0 checks:
```bash
./scripts/run-phase0-automated-checks.sh
```

Run browser benchmark only:
```bash
./scripts/lsp-phase0-benchmark.sh
```

Run one non-blocking iteration (for quick smoke):
```bash
ENFORCE_THRESHOLD=0 ITERATIONS=1 ./scripts/lsp-phase0-benchmark.sh
```

## Baseline Result Captured During Implementation
A baseline run was executed and artifacts were generated:
- `target/lsp-phase0/metrics.jsonl`
- `target/lsp-phase0/summary.json`

## Root Cause and Fix for `initialize` Failures

### Root cause
The benchmark client initially timed out `initialize` too early relative to current bridge startup behavior:
- server bridge startup path can take ~15s in current implementation
- benchmark request timeout was initially lower than that
- client closed early, causing bridge startup interruption logs (`stage=bridgeStart`, `Interrupted while waiting for JDT LS startup`)

There was also a workspace mismatch in the benchmark URI path that made diagnostics capture unreliable.

### Fixes applied
- Added richer startup failure diagnostics in `LspBridge`:
  - stderr tail summary
  - runtime diagnostics (`JAVA_HOME`, `PATH`, `which java`, `java -version`, binary permissions)
- Explicitly set Java env in `src/main/docker/Dockerfile.lsp`:
  - `ENV JAVA_HOME=/opt/java/openjdk`
  - `ENV PATH="${JAVA_HOME}/bin:${PATH}"`
- Rebuilt LSP image via `./build-lsp-docker-image.sh`
- Updated benchmark timeouts to allow full startup cycle:
  - WebDriver script timeout and LSP request timeout increased
- Aligned benchmark workspace URI to server/session path:
  - `file:///tmp/workspaces/test_<JSESSIONID>/project`
- Added active diagnostics request in benchmark flow (`textDocument/diagnostic`)

### Verification result after fix
Full 10-iteration re-baseline passed:
- `iterationsCaptured`: 10
- `completeMetricIterations`: 10
- `successRate`: 1.0
- `thresholdPass`: true

Latest summary metrics (from `target/lsp-phase0/summary.json`):
- `wsOpenMs`: median `7`, p95 `54`
- `initializeMs`: median `17421`, p95 `20040`
- `firstDiagnosticsMs`: median `21387`, p95 `23631`
- `firstCompletionMs`: median `21300.5`, p95 `23542`

## Checklist Status
Phase 0 entries in:
- `LSP_PERFORMANCE_EXECUTION_CHECKLIST.md`

were updated to checked for implemented automation tasks and phase exit criteria.
