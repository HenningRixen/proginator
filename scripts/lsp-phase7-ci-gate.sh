#!/usr/bin/env bash
set -euo pipefail

ITERATIONS="${ITERATIONS:-10}"
BENCH_MODE="${BENCH_MODE:-cold}"
MAVEN_REPO_LOCAL="${MAVEN_REPO_LOCAL:-.m2-local}"
OUT_DIR="${OUT_DIR:-target/lsp-phase7-ci}"
INIT_P95_BUDGET_MS="${INIT_P95_BUDGET_MS:-3000}"
DIAG_P95_BUDGET_MS="${DIAG_P95_BUDGET_MS:-4000}"
COMP_P95_BUDGET_MS="${COMP_P95_BUDGET_MS:-4500}"
MIN_SUCCESS_RATE="${MIN_SUCCESS_RATE:-0.99}"

mkdir -p "${OUT_DIR}"

ITERATIONS="${ITERATIONS}" \
BENCH_MODE="${BENCH_MODE}" \
OUT_DIR="${OUT_DIR}" \
MAVEN_REPO_LOCAL="${MAVEN_REPO_LOCAL}" \
ENFORCE_THRESHOLD=0 \
./scripts/lsp-phase0-benchmark.sh

python3 - <<'PY' "${OUT_DIR}/summary.json" "${INIT_P95_BUDGET_MS}" "${DIAG_P95_BUDGET_MS}" "${COMP_P95_BUDGET_MS}" "${MIN_SUCCESS_RATE}"
import json
import sys

summary_path = sys.argv[1]
init_budget = float(sys.argv[2])
diag_budget = float(sys.argv[3])
comp_budget = float(sys.argv[4])
min_success = float(sys.argv[5])

with open(summary_path, "r", encoding="utf-8") as fh:
    summary = json.load(fh)

def p95(metric):
    return summary.get("metrics", {}).get(metric, {}).get("p95")

init_p95 = p95("initializeMs")
diag_p95 = p95("firstDiagnosticsMs")
comp_p95 = p95("firstCompletionMs")
success_rate = summary.get("successRate", 0.0)

checks = {
    "successRate": success_rate >= min_success,
    "initializeP95": isinstance(init_p95, (int, float)) and init_p95 <= init_budget,
    "diagnosticsP95": isinstance(diag_p95, (int, float)) and diag_p95 <= diag_budget,
    "completionP95": isinstance(comp_p95, (int, float)) and comp_p95 <= comp_budget
}

result = {
    "summaryPath": summary_path,
    "thresholds": {
        "minSuccessRate": min_success,
        "initializeP95MaxMs": init_budget,
        "diagnosticsP95MaxMs": diag_budget,
        "completionP95MaxMs": comp_budget
    },
    "actual": {
        "successRate": success_rate,
        "initializeP95": init_p95,
        "diagnosticsP95": diag_p95,
        "completionP95": comp_p95
    },
    "checks": checks,
    "pass": all(checks.values())
}

print(json.dumps(result, indent=2))
if not result["pass"]:
    sys.exit(2)
PY

echo "Phase 7 CI gate passed."
