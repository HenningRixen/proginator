#!/usr/bin/env bash
set -euo pipefail

ITERATIONS="${ITERATIONS:-6}"
BENCH_MODE="${BENCH_MODE:-semi-cold}"
MAVEN_REPO_LOCAL="${MAVEN_REPO_LOCAL:-.m2-local}"
BASE_DIR="${BASE_DIR:-target/lsp-phase7-matrix}"
MEMORY_SET="${MEMORY_SET:-512 768 1024}"
CPU_SET="${CPU_SET:-1.5 2.0}"

mkdir -p "${BASE_DIR}"

echo "Running Phase 7 resource matrix iterations=${ITERATIONS} mode=${BENCH_MODE}"

for mem in ${MEMORY_SET}; do
  for cpu in ${CPU_SET}; do
    out_dir="${BASE_DIR}/m${mem}-c${cpu}"
    echo "Case memory=${mem} cpu=${cpu}"
    ITERATIONS="${ITERATIONS}" \
    BENCH_MODE="${BENCH_MODE}" \
    OUT_DIR="${out_dir}" \
    MAVEN_REPO_LOCAL="${MAVEN_REPO_LOCAL}" \
    ENFORCE_THRESHOLD=0 \
    APP_JVM_ARGS_EXTRA="-Dapp.lsp.memory-mb=${mem} -Dapp.lsp.cpus=${cpu}" \
    ./scripts/lsp-phase0-benchmark.sh
  done
done

python3 - <<'PY' "${BASE_DIR}"
import glob
import json
import os
import sys

base_dir = sys.argv[1]
rows = []
for path in sorted(glob.glob(os.path.join(base_dir, "m*-c*/summary.json"))):
    case = os.path.basename(os.path.dirname(path))
    with open(path, "r", encoding="utf-8") as fh:
        summary = json.load(fh)
    rows.append({
        "case": case,
        "mode": summary.get("mode"),
        "successRate": summary.get("successRate"),
        "initializeP95": summary.get("metrics", {}).get("initializeMs", {}).get("p95"),
        "diagnosticsP95": summary.get("metrics", {}).get("firstDiagnosticsMs", {}).get("p95"),
        "completionP95": summary.get("metrics", {}).get("firstCompletionMs", {}).get("p95"),
        "containerDiagnostics": summary.get("containerDiagnostics", {})
    })

report = {"cases": rows}
out_path = os.path.join(base_dir, "matrix-summary.json")
with open(out_path, "w", encoding="utf-8") as fh:
    json.dump(report, fh, indent=2)

print(json.dumps(report, indent=2))
PY

echo "Phase 7 resource matrix complete. Summary: ${BASE_DIR}/matrix-summary.json"
