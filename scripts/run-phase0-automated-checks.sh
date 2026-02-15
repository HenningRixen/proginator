#!/usr/bin/env bash
set -euo pipefail

MAVEN_REPO_LOCAL="${MAVEN_REPO_LOCAL:-.m2-local}"

echo "Running Phase 0 backend observability tests..."
./mvnw -q -Dmaven.repo.local="${MAVEN_REPO_LOCAL}" -Dtest=LspHealthControllerIntegrationTest,LspWebSocketObservabilityIntegrationTest test

echo "Running Phase 0 browser benchmark..."
MAVEN_REPO_LOCAL="${MAVEN_REPO_LOCAL}" ./scripts/lsp-phase0-benchmark.sh

echo "Phase 0 automated checks completed."
