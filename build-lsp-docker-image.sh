#!/bin/bash
# Build Docker image for Java LSP (Eclipse JDT LS)

set -e

echo "Building Docker image for Java LSP..."

cd "$(dirname "$0")"

docker build -t proginator-jdtls -f src/main/docker/Dockerfile.lsp .

echo "Docker image built successfully: proginator-jdtls"
