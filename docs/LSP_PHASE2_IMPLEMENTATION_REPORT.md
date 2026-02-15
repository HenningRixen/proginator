# LSP Phase 2 Implementation Report

## Scope
Phase 2 implemented **Option A** from the execution checklist:
- persist one running JDT LS process per HTTP session workspace key
- reuse it across WebSocket reconnects

Reference docs used during implementation:
- `docs/LSP_FEATURE.md`
- `docs/LSP_PHASE0_AUTOMATION_REPORT.md`
- `LSP_PERFORMANCE_EXECUTION_CHECKLIST.md`

## Changes Implemented

### 1. Workspace-level bridge reuse in session manager
Updated `src/main/java/com/example/prog1learnapp/service/lsp/LspSessionManager.java`:
- added workspace-level bridge registry: `workspaceKey -> bridge record`
- reattach logic:
  - if bridge for workspace exists, attach new WS session instead of creating new bridge/process
- startup logic:
  - only create/start bridge on first session for a workspace
- idle cleanup logic:
  - scheduled cleanup removes idle workspace bridges after TTL
  - removes container via `containerService.forceRemove(workspaceKey)`

### 2. Bridge lifecycle changed from WS-bound to attach/detach model
Updated `src/main/java/com/example/prog1learnapp/service/lsp/LspBridge.java`:
- removed single `webSocketSession` ownership
- added attached session map and APIs:
  - `attachSession(...)`
  - `detachSession(...)`
  - `getAttachedSessionCount()`
  - `getLastAttachedEpochMs()`
- output forwarding now broadcasts to currently attached sessions
- process is no longer closed on each WS close; it is closed on manager cleanup/shutdown

### 3. Factory for testable bridge creation
Added `src/main/java/com/example/prog1learnapp/service/lsp/LspBridgeFactory.java`:
- centralizes `LspBridge` construction
- enables deterministic unit tests for `LspSessionManager` without requiring Docker process startup

## Tests Added / Updated

### New tests
- `src/test/java/com/example/prog1learnapp/service/lsp/LspSessionManagerTest.java`
  - verifies reuse for same workspace (single acquire/start, multiple attaches)
  - verifies detach + idle cleanup closes bridge and removes container mapping
  - verifies open fails when no container available

### Updated tests
- `src/test/java/com/example/prog1learnapp/controller/lsp/LspWebSocketObservabilityIntegrationTest.java`
  - updated constructor usage for new `LspSessionManager` dependency (`LspBridgeFactory`)

### Existing Phase 1 startup tests still valid
- `src/test/java/com/example/prog1learnapp/service/lsp/LspBridgeStartupTest.java`

### Test command run
```bash
./mvnw -q -Dtest=LspSessionManagerTest,LspBridgeStartupTest,LspHealthControllerIntegrationTest,LspWebSocketObservabilityIntegrationTest test
```

## Performance Validation

Benchmark command:
```bash
ITERATIONS=10 MAVEN_REPO_LOCAL=/home/hr/.m2/repository ./scripts/lsp-phase0-benchmark.sh
```

Result (`target/lsp-phase0/summary.json`):
- `iterationsCaptured`: 10
- `completeMetricIterations`: 10
- `successRate`: 1.0
- `thresholdPass`: true

Metrics:
- `wsOpenMs`: median `7`, p95 `69`
- `initializeMs`: median `6.5`, p95 `15532`
- `firstDiagnosticsMs`: median `138`, p95 `18125`
- `firstCompletionMs`: median `133.5`, p95 `18332`

Interpretation:
- first iteration is cold start (high values)
- subsequent warm reconnects are fast (single-digit ms initialize, ~sub-second diagnostics/completion)
- this matches Phase 2 objective for warm-path reuse

## Checklist Status
Phase 2 section in `LSP_PERFORMANCE_EXECUTION_CHECKLIST.md` is marked complete, including:
- objectives
- code touchpoints
- test cases
- exit criteria
