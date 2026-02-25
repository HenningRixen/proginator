#!/usr/bin/env bash
set -euo pipefail

ITERATIONS="${ITERATIONS:-8}"
BENCH_MODE="${BENCH_MODE:-semi-cold}"
MAVEN_REPO_LOCAL="${MAVEN_REPO_LOCAL:-.m2-local}"
BASE_DIR="${BASE_DIR:-target/lsp-phase6}"
OFF_DIR="${BASE_DIR}/off"
ON_DIR="${BASE_DIR}/on"
COMPARE_JSON="${BASE_DIR}/compare-summary.json"

mkdir -p "${BASE_DIR}"
rm -f "${COMPARE_JSON}"

echo "Running baseline benchmark with prewarm OFF..."
ITERATIONS="${ITERATIONS}" \
BENCH_MODE="${BENCH_MODE}" \
OUT_DIR="${OFF_DIR}" \
MAVEN_REPO_LOCAL="${MAVEN_REPO_LOCAL}" \
ENFORCE_THRESHOLD=0 \
APP_JVM_ARGS_EXTRA="-Dapp.lsp.prewarm-on-login=false" \
./scripts/lsp-phase0-benchmark.sh

echo "Running benchmark with prewarm ON..."
ITERATIONS="${ITERATIONS}" \
BENCH_MODE="${BENCH_MODE}" \
OUT_DIR="${ON_DIR}" \
MAVEN_REPO_LOCAL="${MAVEN_REPO_LOCAL}" \
ENFORCE_THRESHOLD=0 \
APP_JVM_ARGS_EXTRA="-Dapp.lsp.prewarm-on-login=true" \
./scripts/lsp-phase0-benchmark.sh

python3 - <<'PY' "${OFF_DIR}/summary.json" "${ON_DIR}/summary.json" "${COMPARE_JSON}"
import json
import sys

off_path, on_path, out_path = sys.argv[1], sys.argv[2], sys.argv[3]

with open(off_path, "r", encoding="utf-8") as fh:
    off = json.load(fh)
with open(on_path, "r", encoding="utf-8") as fh:
    on = json.load(fh)

def median(summary, key):
    return summary.get("metrics", {}).get(key, {}).get("median")

result = {
    "mode": off.get("mode") or on.get("mode") or "unknown",
    "off": off,
    "on": on,
    "delta": {
        "initializeMsMedian": None,
        "firstDiagnosticsMsMedian": None,
        "firstCompletionMsMedian": None,
        "completionRequestMsMedian": None,
        "diagnosticRequestMsMedian": None
    }
}

for metric_key, delta_key in [
    ("initializeMs", "initializeMsMedian"),
    ("firstDiagnosticsMs", "firstDiagnosticsMsMedian"),
    ("firstCompletionMs", "firstCompletionMsMedian"),
    ("completionRequestMs", "completionRequestMsMedian"),
    ("diagnosticRequestMs", "diagnosticRequestMsMedian")
]:
    off_m = median(off, metric_key)
    on_m = median(on, metric_key)
    if isinstance(off_m, (int, float)) and isinstance(on_m, (int, float)):
        result["delta"][delta_key] = on_m - off_m

with open(out_path, "w", encoding="utf-8") as fh:
    json.dump(result, fh, indent=2)

print(json.dumps(result, indent=2))
PY

echo "Prewarm compare summary written to ${COMPARE_JSON}"
