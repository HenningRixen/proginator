# LSP Feature - Technical Documentation

## 1. Purpose and Scope
This document describes the Java LSP integration used in interactive exercises.

The feature provides:
- Java diagnostics in the Monaco editor
- Java code completion
- Hover information
- Signature help

Implementation is based on:
- Monaco editor in the browser
- Spring WebSocket transport endpoint (`/api/lsp/ws`)
- Per-session bridge to Eclipse JDT LS running inside Docker containers

## 2. End-to-End Architecture
Flow for one interactive exercise page:

1. User opens `/exercise/{id}`.
2. `LearnController` injects `lspEnabled` and `lspWorkspaceUri` into the template model.
3. `exercise.html` initializes Monaco and, if enabled, calls `window.initExerciseLsp(...)`.
4. `exercise-lsp.js` opens WebSocket connection to `/api/lsp/ws`.
5. `LspWebSocketHandler` authenticates/opens session bridge via `LspSessionManager`.
6. `LspSessionManager` acquires or creates a Docker container through `JdtLsContainerService`.
7. `LspBridge` starts JDT LS in that container using `docker exec`, and forwards JSON-RPC messages both ways.
8. Monaco providers issue LSP requests (`completion`, `hover`, `signatureHelp`) and consume diagnostics.

## 3. Backend Components

### 3.1 Configuration Properties
File: `src/main/java/com/example/prog1learnapp/config/lsp/LspProperties.java`

Properties under prefix `app.lsp`:
- `enabled` (default `false` in class; overridden in YAML)
- `image` (default `proginator-jdtls`)
- `connectTimeoutMs` (default `15000`)
- `idleTtlSeconds` (default `300`)
- `maxSessions` (default `50`)
- `memoryMb` (default `512`)
- `cpus` (default `"1.0"`)
- `maxMessageBytes` (default `1000000`)
- `cleanupIntervalMs` (default `30000`)
- `allowedOrigins` (default localhost patterns)

### 3.2 WebSocket Registration
File: `src/main/java/com/example/prog1learnapp/config/lsp/LspWebSocketConfig.java`

- Registers handler at `/api/lsp/ws`.
- Adds `HttpSessionHandshakeInterceptor` so HTTP session ID is available in WS attributes.
- Applies allowed origin patterns from `app.lsp.allowed-origins`.

### 3.3 WebSocket Handler
File: `src/main/java/com/example/prog1learnapp/controller/lsp/LspWebSocketHandler.java`

Behavior:
- Rejects connection when LSP is disabled (`POLICY_VIOLATION`).
- Rejects unauthenticated WS sessions (`NOT_ACCEPTABLE`).
- On successful connect, delegates to `LspSessionManager.open(...)`.
- For each text frame, forwards payload to bridge; oversized/invalid payload closes with `BAD_DATA`.
- On transport error/close, always delegates cleanup to `LspSessionManager.close(...)`.

### 3.4 Session and Bridge Management
File: `src/main/java/com/example/prog1learnapp/service/lsp/LspSessionManager.java`

Key behavior:
- Derives `workspaceKey` from `principalName:httpSessionId` (fallback to WS ID).
- Acquires container by `workspaceKey`.
- Creates one `LspBridge` per WS connection.
- Enforces payload size limit using `app.lsp.max-message-bytes`.
- Tracks maps:
  - WS ID -> `LspBridge`
  - WS ID -> `workspaceKey`
- On close, bridge is destroyed and container reference count is decremented.

### 3.5 Docker Container Pool/Lifecycle
File: `src/main/java/com/example/prog1learnapp/service/lsp/JdtLsContainerService.java`

Lifecycle details:
- On startup (`@PostConstruct`):
  - If disabled: marks unavailable.
  - If enabled: checks Docker CLI with `docker version`.
- `acquireContainer(sessionKey)`:
  - Reuses existing session container and increments `refCount`.
  - Creates new container if not existing and below `maxSessions`.
- `releaseContainer(sessionKey)`:
  - Decrements `refCount`.
  - Updates last-used timestamp.
- Scheduled cleanup (`@Scheduled`):
  - Removes containers idle for longer than `idleTtlSeconds` when `refCount == 0`.
- Shutdown (`@PreDestroy`):
  - Removes all tracked containers.

Container hardening flags (from `docker create`):
- `--network=none`
- `--read-only`
- `--tmpfs /tmp`
- `--tmpfs /home/runner/.eclipse`
- `--security-opt=no-new-privileges`
- `--cap-drop=ALL`
- `--user=runner`
- resource caps via memory/cpu/pids

### 3.6 JDT LS Process Bridge
File: `src/main/java/com/example/prog1learnapp/service/lsp/LspBridge.java`

Behavior:
- Starts JDT LS inside target container with:
  - workspace data dir: `/tmp/workspaces/<sanitizedWorkspaceKey>`
  - fallback binary discovery (`/opt/jdtls/bin/jdtls`, `/opt/jdtls/jdtls`, `jdtls` in PATH)
- Uses `docker exec -i ... sh -c "<startCommand>"`.
- Writes client JSON payloads using LSP framing (`Content-Length`).
- Reads server messages from stdout and sends them to WS client.
- Drains stderr into application logs (`JDT LS [containerName]: ...`).
- Waits `connectTimeoutMs` for early process failure detection.

### 3.7 JSON-RPC Framing Utility
File: `src/main/java/com/example/prog1learnapp/service/lsp/LspJsonRpcFraming.java`

Responsibilities:
- `writeMessage(...)` writes `Content-Length` header + UTF-8 body.
- `readMessage(...)` parses headers until blank line, reads exact byte length body.
- Throws on missing `Content-Length`.

### 3.8 Health Endpoint
File: `src/main/java/com/example/prog1learnapp/controller/lsp/LspHealthController.java`

Endpoint: `GET /api/lsp/health`

Payload fields:
- `enabled`
- `dockerAvailable`
- `imageAvailable`
- `activeContainers`
- `activeBridges`

Status:
- `200 OK` only when enabled + Docker available + image available.
- otherwise `503 Service Unavailable`.

## 4. Frontend Integration

### 4.1 Template Wiring
Files:
- `src/main/resources/templates/exercise.html`
- `src/main/java/com/example/prog1learnapp/controller/LearnController.java`

LSP script is only included for interactive exercises (`exercise.interactive`).

`LearnController.exercise(...)` injects:
- `lspEnabled`: from `app.lsp.enabled`
- `lspWorkspaceUri`: `file:///tmp/workspaces/test_<httpSessionId>/project`

Client initialization (in template):
- Starts Monaco.
- If `lspEnabled`, calls `window.initExerciseLsp({ enabled, editor, exerciseId, workspaceUri })`.

### 4.2 Client Protocol Behavior
File: `src/main/resources/static/js/exercise-lsp.js`

Key client steps:
- Creates/sets Monaco model URI:
  - `<workspaceUri>/src/Exercise<exerciseId>.java`
- Opens WebSocket `/api/lsp/ws`.
- Sends `initialize` request.
- Sends `initialized` notification.
- Sends `textDocument/didOpen`.
- Debounced `textDocument/didChange` on content edits (250 ms).
- Sends `textDocument/didClose` during dispose when session is ready.

Registered Monaco providers:
- completion:
  - triggers `.` `(` `,`
  - maps LSP completion kinds to Monaco kinds
- hover:
  - requests `textDocument/hover`
  - normalizes markdown payload
- signature help:
  - triggers `(` `,`
  - requests `textDocument/signatureHelp`

Diagnostics:
- consumes `textDocument/publishDiagnostics`
- maps to Monaco markers with `jdtls` owner

Timeout behavior:
- each request times out client-side after 4 seconds and resolves `null`.

Failure behavior:
- if init fails, client returns `null`; page continues without LSP.
- optional notification: `Java-Hinweise nicht verfuegbar. Editor laeuft ohne LSP.`

## 5. Docker Image and Runtime Prerequisites

### 5.1 LSP Docker Image
Files:
- `src/main/docker/Dockerfile.lsp`
- `build-lsp-docker-image.sh`

Image build:
- Base: `eclipse-temurin:21-jre`
- Installs `curl`, `python3`, `tar`
- Downloads Eclipse JDT LS milestone archive (`JDTLS_VERSION` default `1.55.0`)
- Exposes binary as `jdtls`
- Creates non-root user `runner`
- Prepares `/tmp/workspaces`

Build command:
- `./build-lsp-docker-image.sh`

Resulting image name:
- `proginator-jdtls`

### 5.2 App Scheduling Requirement
File: `src/main/java/com/example/prog1learnapp/Prog1LearnApp.java`

- `@EnableScheduling` is enabled globally.
- Required for periodic idle-container cleanup in `JdtLsContainerService`.

## 6. Configuration by Profile

### 6.1 Base config (`application.yml`)
Default runtime includes LSP enabled with full settings:
- `app.lsp.enabled: true`
- image/resources/session limits/origins explicitly defined

### 6.2 Dev config (`application-dev.yml`)
- Keeps LSP enabled (`app.lsp.enabled: true`).
- Does not override the rest of base LSP settings.

### 6.3 Prod config (`application-prod.yml`)
- Explicitly disables LSP: `app.lsp.enabled: false`

## 7. Security Model

### 7.1 Route Protection
File: `src/main/java/com/example/prog1learnapp/config/SecurityConfig.java`

- LSP endpoints are not whitelisted.
- Therefore `/api/lsp/ws` and `/api/lsp/health` require authentication via default `anyRequest().authenticated()`.

### 7.2 Additional WS Checks
`LspWebSocketHandler` enforces:
- feature enabled check
- non-null authenticated principal

### 7.3 Container Isolation
LSP backend containers run with strong restrictions:
- no network
- reduced Linux capabilities
- read-only rootfs
- non-root user
- limited tmpfs writable paths

## 8. Operational Notes and Current Limitations

1. No automated tests currently target the LSP feature classes or `/api/lsp/*` behavior.
2. `LspSessionManager` workspace key and template-provided `lspWorkspaceUri` are generated differently; current implementation still works because URI is client metadata while backend chooses its own workspace key.
3. The browser-side request timeout is hardcoded to 4 seconds.
4. Health endpoint status is purely infrastructure-based and does not verify a live JDT LS handshake.

## 9. Feature File Map

Backend:
- `src/main/java/com/example/prog1learnapp/config/lsp/LspProperties.java`
- `src/main/java/com/example/prog1learnapp/config/lsp/LspWebSocketConfig.java`
- `src/main/java/com/example/prog1learnapp/controller/lsp/LspWebSocketHandler.java`
- `src/main/java/com/example/prog1learnapp/controller/lsp/LspHealthController.java`
- `src/main/java/com/example/prog1learnapp/service/lsp/JdtLsContainerService.java`
- `src/main/java/com/example/prog1learnapp/service/lsp/LspSessionManager.java`
- `src/main/java/com/example/prog1learnapp/service/lsp/LspBridge.java`
- `src/main/java/com/example/prog1learnapp/service/lsp/LspJsonRpcFraming.java`

Frontend:
- `src/main/resources/static/js/exercise-lsp.js`
- `src/main/resources/templates/exercise.html`
- `src/main/java/com/example/prog1learnapp/controller/LearnController.java`

Infrastructure/config:
- `src/main/docker/Dockerfile.lsp`
- `build-lsp-docker-image.sh`
- `src/main/resources/application.yml`
- `src/main/resources/application-dev.yml`
- `src/main/resources/application-prod.yml`
- `src/main/java/com/example/prog1learnapp/Prog1LearnApp.java`
- `src/main/java/com/example/prog1learnapp/config/SecurityConfig.java`
