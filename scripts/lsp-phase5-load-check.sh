#!/usr/bin/env bash
set -euo pipefail

USERS="${USERS:-8}"
PORT="${PORT:-18080}"
BASE_URL="http://127.0.0.1:${PORT}"
CHROMEDRIVER_PORT="${CHROMEDRIVER_PORT:-9515}"
OUT_DIR="${OUT_DIR:-target/lsp-phase5}"
MAVEN_REPO_LOCAL="${MAVEN_REPO_LOCAL:-.m2-local}"
APP_LOG="${OUT_DIR}/app.log"
CHROMEDRIVER_LOG="${OUT_DIR}/chromedriver.log"
RAW_JSON="${OUT_DIR}/load-raw.json"
SUMMARY_JSON="${OUT_DIR}/load-summary.json"
CONTAINER_DIAG_JSON="${OUT_DIR}/container-diagnostics.json"

mkdir -p "${OUT_DIR}"
rm -f "${RAW_JSON}" "${SUMMARY_JSON}" "${CONTAINER_DIAG_JSON}"

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
  ./mvnw -q spring-boot:run \
    -Dmaven.repo.local="${MAVEN_REPO_LOCAL}" \
    -Dspring-boot.run.profiles=dev \
    -Dspring-boot.run.jvmArguments="-Dserver.port=${PORT}" \
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
  wait_for_ready_state_complete
}

open_interactive_exercise() {
  local lesson_url
  lesson_url="$(wd_exec_sync 'const links=[...document.querySelectorAll("a[href*=\"/lesson/\"]")]; return links.length?links[0].href:null;' | jq -r '.value // empty')"
  if [[ -z "${lesson_url}" || "${lesson_url}" == "null" ]]; then
    echo "Could not locate lesson link on dashboard" >&2
    exit 1
  fi
  wd_set_url "${lesson_url}"
  wait_for_ready_state_complete

  local exercise_url
  exercise_url="$(wd_exec_sync 'const links=[...document.querySelectorAll("a[href*=\"/exercise/\"]")]; return links.length?links[0].href:null;' | jq -r '.value // empty')"
  if [[ -z "${exercise_url}" || "${exercise_url}" == "null" ]]; then
    echo "Could not locate exercise link on lesson page" >&2
    exit 1
  fi
  wd_set_url "${exercise_url}"
  wait_for_ready_state_complete
}

run_concurrent_lsp_clients() {
  local js
  js="const done = arguments[0];
const users = ${USERS};
const wsUrl = (location.protocol === 'https:' ? 'wss' : 'ws') + '://' + location.host + '/api/lsp/ws';
const workspaceBase = 'file:///tmp/workspaces/phase5-load';
const source = 'public class Main { public static void main(String[] args){ System.out.println(\\\"x\\\"); } }';

function median(values) {
  if (!values.length) return null;
  const sorted = [...values].sort((a,b)=>a-b);
  const mid = Math.floor(sorted.length / 2);
  return sorted.length % 2 === 0 ? (sorted[mid - 1] + sorted[mid]) / 2 : sorted[mid];
}

function p95(values) {
  if (!values.length) return null;
  const sorted = [...values].sort((a,b)=>a-b);
  const idx = Math.max(0, Math.ceil(sorted.length * 0.95) - 1);
  return sorted[idx];
}

function runClient(index) {
  return new Promise((resolve) => {
    const started = performance.now();
    const ws = new WebSocket(wsUrl);
    let reqId = 1;
    const pending = new Map();
    const result = { user: index, ok: false, wsOpenMs: null, initializeMs: null, completionMs: null, error: null };

    function send(payload) { ws.send(JSON.stringify(payload)); }
    function request(method, params, timeoutMs) {
      const id = reqId++;
      send({ jsonrpc: '2.0', id, method, params });
      return new Promise((res) => {
        const to = setTimeout(() => {
          pending.delete(id);
          res(null);
        }, timeoutMs || 15000);
        pending.set(id, (payload) => {
          clearTimeout(to);
          res(payload);
        });
      });
    }

    ws.onmessage = (event) => {
      let payload;
      try { payload = JSON.parse(event.data); } catch (_) { return; }
      if (payload.id !== undefined && pending.has(payload.id)) {
        const resolver = pending.get(payload.id);
        pending.delete(payload.id);
        resolver(payload);
      }
    };

    ws.onerror = () => {
      result.error = 'ws-error';
      resolve(result);
    };

    ws.onopen = async () => {
      result.wsOpenMs = Math.round(performance.now() - started);
      const initStart = performance.now();
      const workspaceUri = workspaceBase + '-' + index;
      const uri = workspaceUri + '/src/Main' + index + '.java';
      const init = await request('initialize', {
        processId: null,
        rootUri: workspaceUri,
        capabilities: { textDocument: { completion: { completionItem: { snippetSupport: true } } } },
        workspaceFolders: [{ uri: workspaceUri, name: 'phase5-load-' + index }]
      }, 30000);
      if (!init || init.error) {
        result.error = 'initialize-timeout';
        try { ws.close(); } catch (_) {}
        resolve(result);
        return;
      }
      result.initializeMs = Math.round(performance.now() - initStart);
      send({ jsonrpc: '2.0', method: 'initialized', params: {} });
      send({ jsonrpc: '2.0', method: 'textDocument/didOpen', params: { textDocument: { uri, languageId: 'java', version: 1, text: source } } });
      const completionStart = performance.now();
      const completion = await request('textDocument/completion', {
        textDocument: { uri },
        position: { line: 0, character: 20 },
        context: { triggerKind: 1 }
      }, 10000);
      result.completionMs = Math.round(performance.now() - completionStart);
      result.ok = !!completion && !completion.error;
      if (!result.ok && !result.error) {
        result.error = 'completion-timeout';
      }
      try { ws.close(); } catch (_) {}
      resolve(result);
    };
  });
}

(async () => {
  const runs = await Promise.all(Array.from({ length: users }, (_, i) => runClient(i + 1)));
  const okRuns = runs.filter((r) => r.ok);
  const ws = okRuns.map((r) => r.wsOpenMs).filter((v) => typeof v === 'number');
  const init = okRuns.map((r) => r.initializeMs).filter((v) => typeof v === 'number');
  const completion = okRuns.map((r) => r.completionMs).filter((v) => typeof v === 'number');
  let health = null;
  try {
    const healthResp = await fetch('/api/lsp/health', { credentials: 'include' });
    const healthJson = await healthResp.json();
    health = { status: healthResp.status, body: healthJson };
  } catch (e) {
    health = { status: null, body: { error: String(e && e.message ? e.message : e) } };
  }
  done({
    usersRequested: users,
    usersCompleted: runs.length,
    okCount: okRuns.length,
    successRate: users ? okRuns.length / users : 0,
    metrics: {
      wsOpenMs: { median: median(ws), p95: p95(ws) },
      initializeMs: { median: median(init), p95: p95(init) },
      completionMs: { median: median(completion), p95: p95(completion) }
    },
    health,
    runs
  });
})();"

  wd_exec_async "${js}" | jq -c '.value'
}

summarize() {
  local raw="$1"
  echo "${raw}" > "${RAW_JSON}"
  python3 - <<'PY' "${RAW_JSON}" "${SUMMARY_JSON}" "${USERS}" "${CONTAINER_DIAG_JSON}"
import json
import sys

raw_path = sys.argv[1]
summary_path = sys.argv[2]
users = int(sys.argv[3])
container_diag_path = sys.argv[4]

with open(raw_path, "r", encoding="utf-8") as fh:
    raw = json.load(fh)
with open(container_diag_path, "r", encoding="utf-8") as fh:
    container_diag = json.load(fh)

success_rate = raw.get("successRate", 0.0)
health = raw.get("health", {})
health_body = health.get("body", {}) if isinstance(health, dict) else {}
saturation = health_body.get("saturation", {}) if isinstance(health_body, dict) else {}

summary = {
    "usersRequested": users,
    "usersCompleted": raw.get("usersCompleted"),
    "okCount": raw.get("okCount"),
    "successRate": success_rate,
    "thresholdPass": success_rate >= 0.9,
    "metrics": raw.get("metrics"),
    "activeContainers": health_body.get("activeContainers"),
    "activeBridges": health_body.get("activeBridges"),
    "saturation": saturation,
    "containerDiagnostics": container_diag
}

with open(summary_path, "w", encoding="utf-8") as fh:
    json.dump(summary, fh, indent=2)

print(json.dumps(summary, indent=2))
if not summary["thresholdPass"]:
    sys.exit(2)
PY
}

collect_container_diagnostics() {
  if ! command -v docker >/dev/null 2>&1; then
    printf '{"available":false,"reason":"docker-cli-missing"}\n' > "${CONTAINER_DIAG_JSON}"
    return
  fi
  local containers
  containers="$(docker ps -a --filter "name=proginator-jdtls-" --format '{{.Names}}' 2>/dev/null || true)"
  if [[ -z "${containers}" ]]; then
    printf '{"available":true,"containers":[],"oomKilledCount":0,"nonZeroExitCount":0}\n' > "${CONTAINER_DIAG_JSON}"
    return
  fi
  {
    echo '{"available":true,"containers":['
    local first=1
    local oom=0
    local nonzero=0
    while IFS= read -r name; do
      [[ -z "${name}" ]] && continue
      local inspect
      inspect="$(docker inspect "${name}" 2>/dev/null | jq -c '.[0].State | {name:"'"${name}"'",status:.Status,exitCode:(.ExitCode//0),oomKilled:(.OOMKilled//false),error:(.Error//"")}' || true)"
      if [[ -z "${inspect}" ]]; then
        continue
      fi
      local exit_code
      exit_code="$(echo "${inspect}" | jq -r '.exitCode // 0')"
      local oom_killed
      oom_killed="$(echo "${inspect}" | jq -r '.oomKilled // false')"
      if [[ "${exit_code}" != "0" ]]; then nonzero=$((nonzero + 1)); fi
      if [[ "${oom_killed}" == "true" ]]; then oom=$((oom + 1)); fi
      if [[ $first -eq 0 ]]; then echo ','; fi
      first=0
      echo "${inspect}"
    done <<< "${containers}"
    echo '],"oomKilledCount":'"${oom}"',"nonZeroExitCount":'"${nonzero}"'}'
  } | tr -d '\n' > "${CONTAINER_DIAG_JSON}"
}

echo "Starting app on ${BASE_URL}"
start_app
echo "Starting chromedriver on port ${CHROMEDRIVER_PORT}"
start_chromedriver
echo "Creating browser session"
create_session
echo "Logging in with dev test user"
login_dev_user
echo "Opening interactive exercise"
open_interactive_exercise
echo "Running concurrent LSP load simulation users=${USERS}"
RESULT="$(run_concurrent_lsp_clients)"
echo "Result: ${RESULT}"
collect_container_diagnostics
echo "Summarizing load metrics"
summarize "${RESULT}"
echo "Phase 5 load check complete. Summary: ${SUMMARY_JSON}"
