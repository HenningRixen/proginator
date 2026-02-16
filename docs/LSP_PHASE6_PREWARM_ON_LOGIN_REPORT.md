# LSP Phase 6 Prewarm on Login Report

## Scope
Implement optional LSP backend prewarm at login to reduce interactive exercise cold-start impact without blocking authentication flow.

Checklist source:
- `LSP_PERFORMANCE_EXECUTION_CHECKLIST.md` (Phase 6 section)

## Implemented Changes

### 1. Feature flags and config
Updated:
- `src/main/java/com/example/prog1learnapp/config/lsp/LspProperties.java`
- `src/main/resources/application.yml`

Added:
- `app.lsp.prewarm-on-login` (default `false`)
- `app.lsp.prewarm-timeout-ms` (default `8000`)

Behavior:
- when disabled, prewarm path is fully no-op.

### 2. Async execution infrastructure
Updated:
- `src/main/java/com/example/prog1learnapp/Prog1LearnApp.java` (`@EnableAsync`)

Added:
- `src/main/java/com/example/prog1learnapp/config/lsp/LspAsyncConfig.java`
  - `lspPrewarmExecutor` thread pool.

### 3. Login success hook
Added:
- `src/main/java/com/example/prog1learnapp/config/LspLoginSuccessHandler.java`

Updated:
- `src/main/java/com/example/prog1learnapp/config/SecurityConfig.java`

Behavior:
- prewarm scheduling is triggered after auth success and before redirect completion.
- scheduling exceptions are caught/logged to avoid login breakage.
- logs include:
  - principal
  - prewarm scheduled boolean
  - login hook duration.

### 4. Prewarm service
Added:
- `src/main/java/com/example/prog1learnapp/service/lsp/LspPrewarmService.java`

Behavior:
- validates feature flag and inputs.
- uses existing workspace key contract (`principal:httpSessionId`) via `LspSessionManager`.
- skips if workspace already warm.
- per-workspace in-flight guard prevents duplicate concurrent prewarm.
- async execution through `lspPrewarmExecutor`.
- structured logs:
  - `start`
  - `skip` (missing data / already warm / in-flight)
  - `success`
  - `fail`

### 5. Session manager prewarm path
Updated:
- `src/main/java/com/example/prog1learnapp/service/lsp/LspSessionManager.java`

Added:
- `workspaceKeyForSession(...)`
- `hasBridgeForWorkspaceKey(...)`
- `prewarmWorkspace(...)`

Behavior:
- reuses existing bridge/container lifecycle and maps.
- prewarm timeout guard uses:
  - `min(connect-timeout-ms, prewarm-timeout-ms)` with minimum 1s guard.
- safe failure cleanup via `forceRemove(...)`.

### 6. Before/after measurement automation
Updated:
- `scripts/lsp-phase0-benchmark.sh`
  - added `APP_JVM_ARGS_EXTRA` support for controlled runtime flag toggles.

Added:
- `scripts/lsp-phase6-prewarm-compare.sh`
  - runs benchmark with prewarm `off` and `on`
  - writes compare output:
    - `target/lsp-phase6/compare-summary.json`
  - computes median deltas for:
    - `initializeMs`
    - `firstDiagnosticsMs`
    - `firstCompletionMs`

## Tests Added

### Service tests
Added:
- `src/test/java/com/example/prog1learnapp/service/lsp/LspPrewarmServiceTest.java`

Covers:
- prewarm disabled => no call
- enabled + in-flight guard => only one execution per workspace key
- existing backend => skip
- failure path => safe handling (no throw to caller)

### Login hook tests
Added:
- `src/test/java/com/example/prog1learnapp/config/LspLoginSuccessHandlerTest.java`

Covers:
- login still redirects to `/dashboard` when prewarm scheduling throws
- prewarm scheduling invoked on successful login

## Verification Commands

Executed:
```bash
./mvnw -q -Dtest=LspPrewarmServiceTest,LspLoginSuccessHandlerTest,LspSessionManagerTest,LspHealthControllerIntegrationTest test
```

Result:
- passed.

## Checklist Status
Phase 6 checklist items were checked as complete for implementation and test tasks.

Current remaining unchecked item:
- `Exercise first initialize / diagnostics improves in measured runs`

## Host Benchmark Comparison Result

Executed on host:
```bash
ITERATIONS=8 MAVEN_REPO_LOCAL="$HOME/.m2/repository" ./scripts/lsp-phase6-prewarm-compare.sh
cat target/lsp-phase6/compare-summary.json
```

Observed summary (`off` vs `on`):
- `off.successRate`: `1.0`
- `on.successRate`: `0.875` (one initialize-timeout iteration)
- median deltas (`on - off`):
  - `initializeMs`: `+2.0`
  - `firstDiagnosticsMs`: `+60.5`
  - `firstCompletionMs`: `+68.5`

Interpretation:
- in this measured run, enabling prewarm-on-login **regressed** first diagnostics/completion medians and reduced complete-metric success rate.
- therefore the Phase 6 improvement criterion is **not met**.

Decision:
- keep `app.lsp.prewarm-on-login=false` as default.
- keep Phase 6 exit criterion `Exercise first initialize / diagnostics improves in measured runs` unchecked.
