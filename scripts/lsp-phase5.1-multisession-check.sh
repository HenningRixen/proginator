#!/usr/bin/env bash
set -euo pipefail

SESSIONS="${SESSIONS:-24}"
PORT="${PORT:-18080}"
BASE_URL="http://127.0.0.1:${PORT}"
CHROMEDRIVER_PORT="${CHROMEDRIVER_PORT:-9515}"
OUT_DIR="${OUT_DIR:-target/lsp-phase5.1}"
MAVEN_REPO_LOCAL="${MAVEN_REPO_LOCAL:-.m2-local}"
HOLD_MS="${HOLD_MS:-12000}"
STAGGER_MS="${STAGGER_MS:-200}"
APP_LOG="${OUT_DIR}/app.log"
CHROMEDRIVER_LOG="${OUT_DIR}/chromedriver.log"
RUNS_JSON="${OUT_DIR}/runs.json"
HEALTH_JSON="${OUT_DIR}/health.json"
CLEANUP_HEALTH_JSON="${OUT_DIR}/cleanup-health.json"
SUMMARY_JSON="${OUT_DIR}/summary.json"
CONTAINER_DIAG_JSON="${OUT_DIR}/container-diagnostics.json"

mkdir -p "${OUT_DIR}"
rm -f "${RUNS_JSON}" "${HEALTH_JSON}" "${CLEANUP_HEALTH_JSON}" "${SUMMARY_JSON}" "${CONTAINER_DIAG_JSON}" "${OUT_DIR}"/result-*.json

APP_PID=""
CHROMEDRIVER_PID=""
declare -a WD_SESSIONS=()
LAST_CREATED_WD_SESSION=""

cleanup() {
  for sid in "${WD_SESSIONS[@]:-}"; do
    curl -sS -X DELETE "http://127.0.0.1:${CHROMEDRIVER_PORT}/session/${sid}" >/dev/null || true
  done
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

wd_post_sid() {
  local sid="$1"
  local path="$2"
  local body="$3"
  curl -sS -X POST "http://127.0.0.1:${CHROMEDRIVER_PORT}/session/${sid}${path}" \
    -H 'Content-Type: application/json' \
    -d "${body}"
}

wd_set_url_sid() {
  local sid="$1"
  local url="$2"
  wd_post_sid "${sid}" "/url" "$(json_body --arg u "${url}" '{url:$u}')" >/dev/null
}

wd_find_sid() {
  local sid="$1"
  local selector="$2"
  local response
  response="$(wd_post_sid "${sid}" "/element" "$(json_body --arg v "${selector}" '{using:"css selector",value:$v}')")"
  echo "${response}" | jq -r '.value["element-6066-11e4-a52e-4f735466cecf"] // .value.ELEMENT // empty'
}

wd_send_keys_sid() {
  local sid="$1"
  local element_id="$2"
  local text="$3"
  wd_post_sid "${sid}" "/element/${element_id}/value" \
    "$(json_body --arg t "${text}" '{text:$t,value:($t|split(""))}')" >/dev/null
}

wd_click_sid() {
  local sid="$1"
  local element_id="$2"
  wd_post_sid "${sid}" "/element/${element_id}/click" '{}' >/dev/null
}

wd_exec_sync_sid() {
  local sid="$1"
  local script="$2"
  wd_post_sid "${sid}" "/execute/sync" "$(jq -cn --arg s "${script}" '{script:$s,args:[]}')"
}

wd_exec_async_sid() {
  local sid="$1"
  local script="$2"
  wd_post_sid "${sid}" "/execute/async" "$(jq -cn --arg s "${script}" '{script:$s,args:[]}')"
}

wait_for_ready_state_complete_sid() {
  local sid="$1"
  local attempts="${2:-60}"
  local i
  for ((i=1; i<=attempts; i++)); do
    local ready
    ready="$(wd_exec_sync_sid "${sid}" 'return document.readyState;' | jq -r '.value // empty')"
    if [[ "${ready}" == "complete" ]]; then
      return 0
    fi
    sleep 0.25
  done
  echo "Timed out waiting for document.readyState=complete sid=${sid}" >&2
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

create_wd_session() {
  local response sid
  response="$(curl -sS -X POST "http://127.0.0.1:${CHROMEDRIVER_PORT}/session" \
    -H 'Content-Type: application/json' \
    -d "$(json_body '{capabilities:{alwaysMatch:{browserName:"chrome",timeouts:{script:180000,pageLoad:120000,implicit:0},"goog:chromeOptions":{args:["--headless=new","--no-sandbox","--disable-dev-shm-usage","--disable-gpu"]}}}}')")"
  sid="$(echo "${response}" | jq -r '.value.sessionId // .sessionId // empty')"
  if [[ -z "${sid}" ]]; then
    echo "Failed to create WebDriver session: ${response}" >&2
    exit 1
  fi
  WD_SESSIONS+=("${sid}")
  LAST_CREATED_WD_SESSION="${sid}"
}

login_dev_user_sid() {
  local sid="$1"
  wd_set_url_sid "${sid}" "${BASE_URL}/login"
  wait_for_ready_state_complete_sid "${sid}"
  local user_el pass_el submit_el
  user_el="$(wd_find_sid "${sid}" 'input[name="username"]')"
  pass_el="$(wd_find_sid "${sid}" 'input[name="password"]')"
  submit_el="$(wd_find_sid "${sid}" 'button[type="submit"]')"
  if [[ -z "${user_el}" || -z "${pass_el}" || -z "${submit_el}" ]]; then
    echo "Could not locate login form elements for sid=${sid}" >&2
    exit 1
  fi
  wd_send_keys_sid "${sid}" "${user_el}" 'test'
  wd_send_keys_sid "${sid}" "${pass_el}" 'test123'
  wd_click_sid "${sid}" "${submit_el}"
  wait_for_ready_state_complete_sid "${sid}"
}

open_interactive_exercise_sid() {
  local sid="$1"
  local lesson_url exercise_url
  lesson_url="$(wd_exec_sync_sid "${sid}" 'const links=[...document.querySelectorAll("a[href*=\"/lesson/\"]")]; return links.length?links[0].href:null;' | jq -r '.value // empty')"
  if [[ -z "${lesson_url}" || "${lesson_url}" == "null" ]]; then
    echo "Could not locate lesson link for sid=${sid}" >&2
    exit 1
  fi
  wd_set_url_sid "${sid}" "${lesson_url}"
  wait_for_ready_state_complete_sid "${sid}"
  exercise_url="$(wd_exec_sync_sid "${sid}" 'const links=[...document.querySelectorAll("a[href*=\"/exercise/\"]")]; return links.length?links[0].href:null;' | jq -r '.value // empty')"
  if [[ -z "${exercise_url}" || "${exercise_url}" == "null" ]]; then
    echo "Could not locate exercise link for sid=${sid}" >&2
    exit 1
  fi
  wd_set_url_sid "${sid}" "${exercise_url}"
  wait_for_ready_state_complete_sid "${sid}"
}

run_lsp_client_sid() {
  local sid="$1"
  local idx="$2"
  local js
  js="const done = arguments[0];
const wsUrl = (location.protocol === 'https:' ? 'wss' : 'ws') + '://' + location.host + '/api/lsp/ws';
const holdMs = ${HOLD_MS};
const source = 'public class Main { public static void main(String[] args){ System.out.println(\\\"x\\\"); } }';
const workspaceRoot = 'file:///tmp/workspaces/phase5_1_session_${idx}';
const uri = workspaceRoot + '/src/Main.java';
const result = { sessionIndex: ${idx}, ok: false, wsOpenMs: null, initializeMs: null, holdMs, error: null };
let reqId = 1;
const pending = new Map();
const started = performance.now();
const ws = new WebSocket(wsUrl);

function send(payload) { ws.send(JSON.stringify(payload)); }
function request(method, params, timeoutMs) {
  const id = reqId++;
  send({ jsonrpc: '2.0', id, method, params });
  return new Promise((resolve) => {
    const to = setTimeout(() => {
      pending.delete(id);
      resolve(null);
    }, timeoutMs || 30000);
    pending.set(id, (payload) => {
      clearTimeout(to);
      resolve(payload);
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
  result.error = result.error || 'ws-error';
  done(result);
};

ws.onopen = async () => {
  result.wsOpenMs = Math.round(performance.now() - started);
  const initStart = performance.now();
  const init = await request('initialize', {
    processId: null,
    rootUri: workspaceRoot,
    capabilities: { textDocument: { completion: { completionItem: { snippetSupport: true } } } },
    workspaceFolders: [{ uri: workspaceRoot, name: 'phase5.1-' + ${idx} }]
  }, 45000);

  if (!init || init.error) {
    result.error = 'initialize-failed';
    try { ws.close(); } catch (_) {}
    done(result);
    return;
  }
  result.initializeMs = Math.round(performance.now() - initStart);
  send({ jsonrpc: '2.0', method: 'initialized', params: {} });
  send({ jsonrpc: '2.0', method: 'textDocument/didOpen', params: { textDocument: { uri, languageId: 'java', version: 1, text: source } } });
  result.ok = true;
  setTimeout(() => {
    try { ws.close(); } catch (_) {}
    done(result);
  }, holdMs);
};"

  wd_exec_async_sid "${sid}" "${js}" | jq -c '.value' > "${OUT_DIR}/result-${idx}.json"
}

fetch_health_sid() {
  local sid="$1"
  local js='const done = arguments[0];
fetch("/api/lsp/health", { credentials: "include" })
  .then(async (resp) => done({ status: resp.status, body: await resp.json() }))
  .catch((e) => done({ status: null, body: { error: String(e && e.message ? e.message : e) } }));'
  wd_exec_async_sid "${sid}" "${js}" | jq -c '.value'
}

summarize() {
  python3 - <<'PY' "${RUNS_JSON}" "${HEALTH_JSON}" "${CLEANUP_HEALTH_JSON}" "${SUMMARY_JSON}" "${SESSIONS}" "${CONTAINER_DIAG_JSON}"
import json
import statistics
import sys
from math import ceil

runs_path, health_path, cleanup_path, summary_path, sessions, container_diag_path = sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4], int(sys.argv[5]), sys.argv[6]

with open(runs_path, "r", encoding="utf-8") as fh:
    runs = json.load(fh)
with open(health_path, "r", encoding="utf-8") as fh:
    health = json.load(fh)
with open(cleanup_path, "r", encoding="utf-8") as fh:
    cleanup_health = json.load(fh)
with open(container_diag_path, "r", encoding="utf-8") as fh:
    container_diag = json.load(fh)

def median(values):
    return statistics.median(values) if values else None

def p95(values):
    if not values:
        return None
    values = sorted(values)
    idx = max(0, ceil(len(values) * 0.95) - 1)
    return values[idx]

ok_runs = [r for r in runs if r.get("ok")]
fail_runs = [r for r in runs if not r.get("ok")]

ws_values = [r["wsOpenMs"] for r in ok_runs if isinstance(r.get("wsOpenMs"), (int, float))]
init_values = [r["initializeMs"] for r in ok_runs if isinstance(r.get("initializeMs"), (int, float))]

health_body = health.get("body", {}) if isinstance(health, dict) else {}
saturation = health_body.get("saturation", {}) if isinstance(health_body, dict) else {}
max_sessions = saturation.get("maxSessions")
reject_count = saturation.get("saturationRejectCount", 0)

success_rate = len(ok_runs) / sessions if sessions else 0.0
saturated_target = isinstance(max_sessions, int) and sessions > max_sessions
expected_rejected = max(0, sessions - max_sessions) if isinstance(max_sessions, int) else 0
expected_min_ok = max(1, max_sessions - 2) if isinstance(max_sessions, int) else 1
expected_min_fail = expected_rejected

if saturated_target:
    # Saturated run should keep most capacity slots successful and reject overflow safely.
    threshold_pass = (
        len(ok_runs) >= expected_min_ok
        and len(fail_runs) >= expected_min_fail
        and reject_count >= expected_rejected
        and health.get("status") == 200
    )
else:
    threshold_pass = success_rate >= 0.90

cleanup_body = cleanup_health.get("body", {}) if isinstance(cleanup_health, dict) else {}
cleanup_active_containers = cleanup_body.get("activeContainers")
cleanup_active_bridges = cleanup_body.get("activeBridges")

summary = {
    "sessionsRequested": sessions,
    "sessionsCompleted": len(runs),
    "okCount": len(ok_runs),
    "failCount": len(fail_runs),
    "successRate": success_rate,
    "saturatedTarget": saturated_target,
    "expectedMinOkWhenSaturated": expected_min_ok if saturated_target else None,
    "expectedMinFailWhenSaturated": expected_min_fail if saturated_target else None,
    "expectedMinRejectsWhenSaturated": expected_rejected if saturated_target else None,
    "thresholdPass": threshold_pass,
    "metrics": {
        "wsOpenMs": {"median": median(ws_values), "p95": p95(ws_values)},
        "initializeMs": {"median": median(init_values), "p95": p95(init_values)}
    },
    "health": health,
    "cleanupHealth": cleanup_health,
    "cleanup": {
        "activeContainersAfterWait": cleanup_active_containers,
        "activeBridgesAfterWait": cleanup_active_bridges
    },
    "saturationRejectCount": reject_count,
    "containerDiagnostics": container_diag
}

with open(summary_path, "w", encoding="utf-8") as fh:
    json.dump(summary, fh, indent=2)

print(json.dumps(summary, indent=2))
if not threshold_pass:
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

for i in $(seq 1 "${SESSIONS}"); do
  create_wd_session
  sid="${LAST_CREATED_WD_SESSION}"
  echo "Preparing session ${i}/${SESSIONS} sid=${sid}"
  login_dev_user_sid "${sid}"
  open_interactive_exercise_sid "${sid}"
done

echo "Launching concurrent LSP sessions count=${SESSIONS} holdMs=${HOLD_MS}"
declare -a pids=()
stagger_seconds="$(printf '%d.%03d' "$((STAGGER_MS / 1000))" "$((STAGGER_MS % 1000))")"
for idx in $(seq 1 "${SESSIONS}"); do
  sid="${WD_SESSIONS[$((idx - 1))]}"
  run_lsp_client_sid "${sid}" "${idx}" &
  pids+=("$!")
  sleep "${stagger_seconds}"
done
for pid in "${pids[@]}"; do
  wait "${pid}"
done

jq -s '.' "${OUT_DIR}"/result-*.json > "${RUNS_JSON}"
fetch_health_sid "${WD_SESSIONS[0]}" > "${HEALTH_JSON}"

echo "Waiting for cleanup window..."
sleep 20
fetch_health_sid "${WD_SESSIONS[0]}" > "${CLEANUP_HEALTH_JSON}"
collect_container_diagnostics

echo "Summarizing results"
summarize
echo "Phase 5.1 multi-session check complete. Summary: ${SUMMARY_JSON}"
