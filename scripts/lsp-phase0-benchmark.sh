#!/usr/bin/env bash
set -euo pipefail

ITERATIONS="${ITERATIONS:-10}"
PORT="${PORT:-8080}"
BASE_URL="http://127.0.0.1:${PORT}"
CHROMEDRIVER_PORT="${CHROMEDRIVER_PORT:-9515}"
OUT_DIR="${OUT_DIR:-target/lsp-phase0}"
MAVEN_REPO_LOCAL="${MAVEN_REPO_LOCAL:-.m2-local}"
APP_JVM_ARGS_EXTRA="${APP_JVM_ARGS_EXTRA:-}"
APP_LOG="${OUT_DIR}/app.log"
CHROMEDRIVER_LOG="${OUT_DIR}/chromedriver.log"
RAW_METRICS_JSONL="${OUT_DIR}/metrics.jsonl"
SUMMARY_JSON="${OUT_DIR}/summary.json"
ENFORCE_THRESHOLD="${ENFORCE_THRESHOLD:-1}"

mkdir -p "${OUT_DIR}"
rm -f "${RAW_METRICS_JSONL}" "${SUMMARY_JSON}"

APP_PID=""
CHROMEDRIVER_PID=""
SESSION_ID=""

cleanup() {
  if [[ -n "${SESSION_ID}" ]]; then
    curl -sS -X DELETE "http://127.0.0.1:${CHROMEDRIVER_PORT}/session/${SESSION_ID}" >/dev/null || true
  fi
  if [[ -n "${CHROMEDRIVER_PID}" ]]; then
    kill "${CHROMEDRIVER_PID}" >/dev/null 2>&1 || true
  fi
  if [[ -n "${APP_PID}" ]]; then
    kill "${APP_PID}" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

wait_for_http() {
  local url="$1"
  local attempts="${2:-120}"
  local sleep_s="${3:-1}"
  local i
  for ((i=1; i<=attempts; i++)); do
    if curl -fsS "${url}" >/dev/null 2>&1; then
      return 0
    fi
    sleep "${sleep_s}"
  done
  echo "Timed out waiting for ${url}" >&2
  return 1
}

wait_for_port() {
  local port="$1"
  local attempts="${2:-60}"
  local sleep_s="${3:-0.5}"
  local i
  for ((i=1; i<=attempts; i++)); do
    if (echo >"/dev/tcp/127.0.0.1/${port}") >/dev/null 2>&1; then
      return 0
    fi
    sleep "${sleep_s}"
  done
  echo "Timed out waiting for localhost:${port}" >&2
  return 1
}

json_body() {
  jq -cn "$@"
}

wd_post() {
  local path="$1"
  local body="$2"
  curl -sS -X POST "http://127.0.0.1:${CHROMEDRIVER_PORT}${path}" \
    -H 'Content-Type: application/json' \
    -d "${body}"
}

wd_get() {
  local path="$1"
  curl -sS -X GET "http://127.0.0.1:${CHROMEDRIVER_PORT}${path}"
}

wd_set_url() {
  local url="$1"
  wd_post "/session/${SESSION_ID}/url" "$(json_body --arg u "${url}" '{url:$u}')" >/dev/null
}

wd_find() {
  local selector="$1"
  local response
  response="$(wd_post "/session/${SESSION_ID}/element" "$(json_body --arg v "${selector}" '{using:"css selector",value:$v}')")"
  echo "${response}" | jq -r '.value["element-6066-11e4-a52e-4f735466cecf"] // .value.ELEMENT // empty'
}

wd_send_keys() {
  local element_id="$1"
  local text="$2"
  wd_post "/session/${SESSION_ID}/element/${element_id}/value" \
    "$(json_body --arg t "${text}" '{text:$t,value:($t|split(""))}')" >/dev/null
}

wd_click() {
  local element_id="$1"
  wd_post "/session/${SESSION_ID}/element/${element_id}/click" '{}' >/dev/null
}

wd_exec_sync() {
  local script="$1"
  wd_post "/session/${SESSION_ID}/execute/sync" "$(jq -cn --arg s "${script}" '{script:$s,args:[]}')"
}

wd_exec_async() {
  local script="$1"
  wd_post "/session/${SESSION_ID}/execute/async" "$(jq -cn --arg s "${script}" '{script:$s,args:[]}')"
}

wait_for_ready_state_complete() {
  local attempts="${1:-60}"
  local i
  for ((i=1; i<=attempts; i++)); do
    local ready
    ready="$(wd_exec_sync 'return document.readyState;' | jq -r '.value // empty')"
    if [[ "${ready}" == "complete" ]]; then
      return 0
    fi
    sleep 0.25
  done
  echo "Timed out waiting for document.readyState=complete" >&2
  return 1
}

start_app() {
  mkdir -p "${MAVEN_REPO_LOCAL}"
  local jvm_args="-Dserver.port=${PORT}"
  if [[ -n "${APP_JVM_ARGS_EXTRA}" ]]; then
    jvm_args="${jvm_args} ${APP_JVM_ARGS_EXTRA}"
  fi
  ./mvnw -q spring-boot:run \
    -Dmaven.repo.local="${MAVEN_REPO_LOCAL}" \
    -Dspring-boot.run.profiles=dev \
    -Dspring-boot.run.jvmArguments="${jvm_args}" \
    >"${APP_LOG}" 2>&1 &
  APP_PID=$!
  wait_for_http "${BASE_URL}/login"
}

start_chromedriver() {
  chromedriver --port="${CHROMEDRIVER_PORT}" >"${CHROMEDRIVER_LOG}" 2>&1 &
  CHROMEDRIVER_PID=$!
  wait_for_port "${CHROMEDRIVER_PORT}"
}

create_session() {
  local response
  response="$(wd_post '/session' "$(json_body '{capabilities:{alwaysMatch:{browserName:"chrome",timeouts:{script:120000,pageLoad:120000,implicit:0},"goog:chromeOptions":{args:["--headless=new","--no-sandbox","--disable-dev-shm-usage","--disable-gpu"]}}}}')")"
  SESSION_ID="$(echo "${response}" | jq -r '.value.sessionId // .sessionId // empty')"
  if [[ -z "${SESSION_ID}" ]]; then
    echo "Failed to create WebDriver session: ${response}" >&2
    exit 1
  fi
}

login_dev_user() {
  wd_set_url "${BASE_URL}/login"
  wait_for_ready_state_complete

  local user_el pass_el submit_el
  user_el="$(wd_find 'input[name="username"]')"
  pass_el="$(wd_find 'input[name="password"]')"
  submit_el="$(wd_find 'button[type="submit"]')"

  if [[ -z "${user_el}" || -z "${pass_el}" || -z "${submit_el}" ]]; then
    echo "Could not locate login form elements" >&2
    exit 1
  fi

  wd_send_keys "${user_el}" 'test'
  wd_send_keys "${pass_el}" 'test123'
  wd_click "${submit_el}"

  local attempts=40
  local i
  for ((i=1; i<=attempts; i++)); do
    local path
    path="$(wd_exec_sync 'return window.location.pathname;' | jq -r '.value // empty')"
    if [[ "${path}" == "/dashboard" ]]; then
      return 0
    fi
    sleep 0.25
  done

  echo "Login did not reach /dashboard" >&2
  exit 1
}

run_single_iteration() {
  local js
  js='const done = arguments[0];
const t0 = performance.now();
const wsUrl = (location.protocol === "https:" ? "wss" : "ws") + "://" + location.host + "/api/lsp/ws";
const ws = new WebSocket(wsUrl);
const jsession = ((document.cookie || "").match(/(?:^|;\s*)JSESSIONID=([^;]+)/) || [])[1] || ("session-" + Date.now());
const workspaceKey = "test:" + jsession;
const sanitizedWorkspaceKey = workspaceKey.replace(/[^a-zA-Z0-9-_]/g, "_");
const workspaceRoot = "file:///tmp/workspaces/" + sanitizedWorkspaceKey + "/project";
const uri = workspaceRoot + "/src/Main.java";
const source = "public class Main { public static void main(String[] args){ System.out.println(\"x\"); System.; } }";
const brokenSource = "public class Main { public static void main(String[] args){ int y = ; } }";
const completionPos = source.indexOf("System.") + "System.".length;
const metrics = { wsOpenMs: null, initializeMs: null, firstDiagnosticsMs: null, firstCompletionMs: null, error: null };
let requestId = 1;
const pending = new Map();

function send(payload) {
  ws.send(JSON.stringify(payload));
}

function request(method, params) {
  const id = requestId++;
  send({ jsonrpc: "2.0", id, method, params });
  return new Promise((resolve) => {
    const timeout = setTimeout(() => {
      pending.delete(id);
      resolve(null);
    }, 30000);
    pending.set(id, (responsePayload) => {
      clearTimeout(timeout);
      resolve(responsePayload);
    });
  });
}

function finish() {
  try { ws.close(); } catch (_) {}
  done(metrics);
}

ws.onerror = () => {
  metrics.error = "ws-error";
  finish();
};

ws.onmessage = (event) => {
  let payload;
  try {
    payload = JSON.parse(event.data);
  } catch (_) {
    return;
  }
  if (payload.id !== undefined && pending.has(payload.id)) {
    const resolver = pending.get(payload.id);
    pending.delete(payload.id);
    resolver(payload);
    return;
  }
  if (payload.method === "textDocument/publishDiagnostics" && payload.params && metrics.firstDiagnosticsMs === null) {
    metrics.firstDiagnosticsMs = Math.round(performance.now() - t0);
  }
};

ws.onopen = async () => {
  metrics.wsOpenMs = Math.round(performance.now() - t0);
  const initStart = performance.now();
  const initResult = await request("initialize", {
    processId: null,
    rootUri: workspaceRoot,
    capabilities: {
      textDocument: {
        completion: { completionItem: { snippetSupport: true } },
        hover: {},
        signatureHelp: {},
        publishDiagnostics: {}
      }
    },
    workspaceFolders: [
      { uri: workspaceRoot, name: "phase0-auto" }
    ],
    clientInfo: { name: "phase0-auto", version: "1.0.0" }
  });

  if (!initResult || initResult.error) {
    metrics.error = "initialize-timeout";
    finish();
    return;
  }

  metrics.initializeMs = Math.round(performance.now() - initStart);
  send({ jsonrpc: "2.0", method: "initialized", params: {} });
  send({
    jsonrpc: "2.0",
    method: "textDocument/didOpen",
    params: {
      textDocument: {
        uri: uri,
        languageId: "java",
        version: 1,
        text: source
      }
    }
  });

  const completionStart = performance.now();
  const completionResult = await request("textDocument/completion", {
    textDocument: { uri: uri },
    position: { line: 0, character: completionPos },
    context: { triggerKind: 1 }
  });
  if (!completionResult || completionResult.error) {
    metrics.error = "completion-timeout";
    finish();
    return;
  }
  metrics.firstCompletionMs = Math.round(performance.now() - t0);

  send({
    jsonrpc: "2.0",
    method: "textDocument/didChange",
    params: {
      textDocument: {
        uri: uri,
        version: 2
      },
      contentChanges: [{ text: brokenSource }]
    }
  });

  const diagnosticRequestResult = await request("textDocument/diagnostic", {
    textDocument: { uri: uri }
  });
  if (diagnosticRequestResult && metrics.firstDiagnosticsMs === null) {
    metrics.firstDiagnosticsMs = Math.round(performance.now() - t0);
  }

  const waitUntil = Date.now() + 30000;
  while (metrics.firstDiagnosticsMs === null && Date.now() < waitUntil) {
    await new Promise((r) => setTimeout(r, 100));
  }
  if (metrics.firstDiagnosticsMs === null) {
    metrics.error = "diagnostics-timeout";
  }
  finish();
};'

  wd_set_url "${BASE_URL}/dashboard"
  wait_for_ready_state_complete
  wd_exec_async "${js}" | jq -c '.value'
}

summarize_results() {
  python3 - <<'PY' "${RAW_METRICS_JSONL}" "${SUMMARY_JSON}" "${ITERATIONS}" "${ENFORCE_THRESHOLD}"
import json
import statistics
import sys
from math import ceil

raw_path = sys.argv[1]
summary_path = sys.argv[2]
iterations = int(sys.argv[3])
enforce_threshold = sys.argv[4] == "1"

rows = []
with open(raw_path, "r", encoding="utf-8") as fh:
    for line in fh:
        line = line.strip()
        if not line:
            continue
        rows.append(json.loads(line))

def metric_values(name):
    return [r[name] for r in rows if isinstance(r.get(name), (int, float))]

def median(values):
    if not values:
        return None
    return statistics.median(values)

def p95(values):
    if not values:
        return None
    values = sorted(values)
    idx = max(0, ceil(len(values) * 0.95) - 1)
    return values[idx]

metrics = {}
for key in ["wsOpenMs", "initializeMs", "firstDiagnosticsMs", "firstCompletionMs"]:
    values = metric_values(key)
    metrics[key] = {
        "count": len(values),
        "median": median(values),
        "p95": p95(values)
    }

complete = 0
for r in rows:
    if all(isinstance(r.get(k), (int, float)) for k in ["wsOpenMs", "initializeMs", "firstDiagnosticsMs", "firstCompletionMs"]):
        complete += 1

success_rate = (complete / iterations) if iterations else 0.0
summary = {
    "iterationsRequested": iterations,
    "iterationsCaptured": len(rows),
    "completeMetricIterations": complete,
    "successRate": success_rate,
    "thresholdPass": success_rate >= 0.90,
    "metrics": metrics,
    "rawFile": raw_path
}

with open(summary_path, "w", encoding="utf-8") as fh:
    json.dump(summary, fh, indent=2)

print(json.dumps(summary, indent=2))
if enforce_threshold and not summary["thresholdPass"]:
    sys.exit(2)
PY
}

echo "Starting app on ${BASE_URL}"
start_app
echo "Starting chromedriver on port ${CHROMEDRIVER_PORT}"
start_chromedriver
echo "Creating browser session"
create_session
echo "Logging in with dev test user"
login_dev_user

echo "Running ${ITERATIONS} iterations"
for i in $(seq 1 "${ITERATIONS}"); do
  result="$(run_single_iteration)"
  echo "${result}" >> "${RAW_METRICS_JSONL}"
  echo "Iteration ${i}: ${result}"
done

echo "Computing summary statistics"
summarize_results

echo "Benchmark complete. Summary: ${SUMMARY_JSON}"
