# LSP Phase 1 Implementation Report

## Scope
Phase 1 goal: remove artificial startup blocking in the LSP bridge path (without implementing Phase 2 warm-process reuse).

## What Was Implemented

### 1. Startup wait refactor
Updated `src/main/java/com/example/prog1learnapp/service/lsp/LspBridge.java`:
- Replaced full-timeout blocking behavior with short startup grace probing:
  - old behavior: wait loop could block up to `connectTimeoutMs` (15s) while process was alive
  - new behavior: `waitForEarlyFailure(process, startupGraceMs)` waits only a short grace interval
- Kept fail-fast behavior:
  - if process exits during grace window, startup fails immediately
- Reduced polling sleep step from `100ms` to `50ms`

### 2. Configurable startup grace
Updated `src/main/java/com/example/prog1learnapp/config/lsp/LspProperties.java`:
- Added `startupGraceMs` property (default `750`)

Updated `src/main/resources/application.yml`:
- Added `app.lsp.startup-grace-ms: 750`

Updated `src/main/java/com/example/prog1learnapp/service/lsp/LspSessionManager.java`:
- Passes `lspProperties.getStartupGraceMs()` into `LspBridge`

## Tests Added/Updated

### Unit tests for startup wait behavior
Added `src/test/java/com/example/prog1learnapp/service/lsp/LspBridgeStartupTest.java`:
- `waitForEarlyFailure_processAliveAfterGrace_returnsQuickly`
- `waitForEarlyFailure_processExitsImmediately_failsFast`

### Regression test for WS close behavior on backend failure
Updated `src/test/java/com/example/prog1learnapp/controller/lsp/LspWebSocketObservabilityIntegrationTest.java`:
- Added `websocketHandler_closesWithServerErrorWhenSessionOpenFails`

### Existing Phase 0 tests rerun
- `LspHealthControllerIntegrationTest`
- `LspWebSocketObservabilityIntegrationTest`

Command used:
```bash
./mvnw -q -Dtest=LspBridgeStartupTest,LspHealthControllerIntegrationTest,LspWebSocketObservabilityIntegrationTest test
```

## Smoke Validation
Ran benchmark smoke and 10-iteration run with current Phase 1 code:
```bash
ENFORCE_THRESHOLD=0 ITERATIONS=3 MAVEN_REPO_LOCAL=/home/hr/.m2/repository ./scripts/lsp-phase0-benchmark.sh
ITERATIONS=10 MAVEN_REPO_LOCAL=/home/hr/.m2/repository ./scripts/lsp-phase0-benchmark.sh
```

Latest 10-iteration summary (`target/lsp-phase0/summary.json`):
- `iterationsCaptured`: 10
- `completeMetricIterations`: 10
- `successRate`: 1.0
- `thresholdPass`: true

## Notes
- Phase 1 removes the **artificial full timeout wait** in bridge startup.
- Remaining higher `initialize` latency is mainly tied to real JDT LS startup/runtime cost and is the primary target for next-phase optimization (warm reuse in Phase 2).
