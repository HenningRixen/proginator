#!/usr/bin/env bash
set -euo pipefail

PORT="${PORT:-18080}"
BASE_URL="http://127.0.0.1:${PORT}"
CHROMEDRIVER_PORT="${CHROMEDRIVER_PORT:-9515}"
OUT_DIR="${OUT_DIR:-target/lsp-phase4}"
MAVEN_REPO_LOCAL="${MAVEN_REPO_LOCAL:-.m2-local}"
APP_LOG="${OUT_DIR}/app.log"
CHROMEDRIVER_LOG="${OUT_DIR}/chromedriver.log"
SUMMARY_JSON="${OUT_DIR}/ux-summary.json"

mkdir -p "${OUT_DIR}"
rm -f "${SUMMARY_JSON}"

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
}

open_interactive_exercise() {
  wait_for_ready_state_complete
  local lesson_urls
  lesson_urls="$(wd_exec_sync 'const links=[...document.querySelectorAll("a[href*=\"/lesson/\"]")].map((a) => a.href); return links;' | jq -r '.value[]?')"
  if [[ -z "${lesson_urls}" ]]; then
    echo "Could not locate lesson links on dashboard" >&2
    exit 1
  fi

  local found_url=""
  local lesson_url
  for lesson_url in ${lesson_urls}; do
    wd_set_url "${lesson_url}"
    wait_for_ready_state_complete

    local exercise_urls
    exercise_urls="$(wd_exec_sync 'const links=[...document.querySelectorAll("a[href*=\"/exercise/\"]")].map((a) => a.href); return links;' | jq -r '.value[]?')"
    if [[ -z "${exercise_urls}" ]]; then
      continue
    fi

    local exercise_url
    for exercise_url in ${exercise_urls}; do
      wd_set_url "${exercise_url}"
      wait_for_ready_state_complete
      local has_editor
      has_editor="$(wd_exec_sync 'return !!document.getElementById("editor");' | jq -r '.value // false')"
      if [[ "${has_editor}" == "true" ]]; then
        found_url="${exercise_url}"
        break
      fi
    done

    if [[ -n "${found_url}" ]]; then
      break
    fi
  done

  if [[ -z "${found_url}" ]]; then
    echo "Could not locate interactive exercise page" >&2
    exit 1
  fi
}

run_ux_checks() {
  local js
  js='const done = arguments[0];
function readIndicatorState() {
  const el = document.getElementById("lsp-status-indicator");
  return el ? (el.getAttribute("data-state") || "") : "";
}
function waitFor(cond, timeoutMs) {
  const started = Date.now();
  return new Promise((resolve) => {
    const timer = setInterval(() => {
      if (cond()) {
        clearInterval(timer);
        resolve(true);
        return;
      }
      if (Date.now() - started > timeoutMs) {
        clearInterval(timer);
        resolve(false);
      }
    }, 100);
  });
}
(async () => {
  const result = {
    health: null,
    normal: {},
    unavailable: {},
    pass: false,
    failures: []
  };

  await waitFor(() => !!window.__lspClientRef && !!window.__exerciseEditor, 20000);
  if (!window.__lspClientRef) {
    result.failures.push("missing-lsp-client");
    done(result);
    return;
  }

  try {
    const healthResp = await fetch("/api/lsp/health", { credentials: "include" });
    const healthPayload = await healthResp.json();
    result.health = {
      status: healthResp.status,
      body: healthPayload
    };
  } catch (e) {
    result.health = {
      status: null,
      body: { error: String(e && e.message ? e.message : e) }
    };
  }

  const beforeState = window.__lspClientRef.getState ? window.__lspClientRef.getState() : null;
  result.normal.beforeState = beforeState;
  result.normal.initialIndicator = readIndicatorState();

  if (window.__exerciseEditor && window.__exerciseEditor.trigger) {
    for (let i = 0; i < 8; i += 1) {
      window.__exerciseEditor.trigger("phase4-ux", "editor.action.triggerSuggest", {});
    }
  }

  await waitFor(() => {
    if (!window.__lspClientRef || !window.__lspClientRef.getState) return false;
    const s = window.__lspClientRef.getState();
    return s === "ready" || s === "failed";
  }, 25000);

  const events = window.__lspClientRef.getStateEvents ? window.__lspClientRef.getStateEvents() : [];
  result.normal.stateEvents = events;
  result.normal.finalState = window.__lspClientRef.getState ? window.__lspClientRef.getState() : null;
  result.normal.indicatorState = readIndicatorState();
  result.normal.completionRequestsSent = window.__lspClientRef.getCompletionRequestsSent
    ? window.__lspClientRef.getCompletionRequestsSent()
    : null;

  if (window.__lspClientRef.debugForceSocketClose) {
    window.__lspClientRef.debugForceSocketClose();
    await waitFor(() => {
      if (!window.__lspClientRef || !window.__lspClientRef.getState) return false;
      const s = window.__lspClientRef.getState();
      return s === "degraded" || s === "failed";
    }, 6000);
  }
  result.normal.afterForcedCloseState = window.__lspClientRef.getState ? window.__lspClientRef.getState() : null;

  if (window.__lspClientRef.dispose) {
    window.__lspClientRef.dispose();
  }
  const exerciseId = Number((window.location.pathname.match(/\/exercise\/(\d+)/) || [])[1] || 1);
  const unavailableClient = window.initExerciseLsp({
    enabled: true,
    editor: window.__exerciseEditor,
    exerciseId: exerciseId,
    workspaceUri: "file:///tmp/workspaces/phase4-unavailable",
    wsUrl: "ws://127.0.0.1:1/unavailable"
  });
  await waitFor(() => unavailableClient && unavailableClient.getState && unavailableClient.getState() === "failed", 8000);
  result.unavailable.state = unavailableClient && unavailableClient.getState ? unavailableClient.getState() : null;
  result.unavailable.indicatorState = readIndicatorState();
  if (unavailableClient && unavailableClient.dispose) {
    unavailableClient.dispose();
  }

  const stateNames = result.normal.stateEvents.map((e) => e.state);
  if (result.health && result.health.status === 200 && result.health.body && result.health.body.dockerAvailable !== false) {
    if (!stateNames.includes("connecting") || !stateNames.includes("ready")) {
      result.failures.push("state-transition");
    }
  } else {
    result.failures.push("backend-unavailable");
  }
  if (!stateNames.includes("connecting")) {
    result.failures.push("state-transition");
  }
  if (typeof result.normal.completionRequestsSent !== "number" || result.normal.completionRequestsSent > 3) {
    result.failures.push("request-throttle");
  }
  if (!(result.normal.afterForcedCloseState === "degraded" || result.normal.afterForcedCloseState === "failed")) {
    result.failures.push("socket-error-transition");
  }
  if (result.unavailable.state !== "failed") {
    result.failures.push("backend-unavailable-transition");
  }

  result.pass = result.failures.length === 0;
  done(result);
})();'

  wd_exec_async "${js}" | jq -c '.value'
}

summarize_results() {
  local raw="$1"
  echo "${raw}" > "${SUMMARY_JSON}"
  local pass
  pass="$(echo "${raw}" | jq -r '.pass')"
  if [[ "${pass}" != "true" ]]; then
    echo "Phase 4 UX checks failed: ${raw}" >&2
    return 2
  fi
  return 0
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
echo "Running Phase 4 UX checks"
RESULT="$(run_ux_checks)"
echo "Result: ${RESULT}"
summarize_results "${RESULT}"
echo "Phase 4 UX checks complete. Summary: ${SUMMARY_JSON}"
