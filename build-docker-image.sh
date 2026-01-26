#!/bin/bash
# Build Docker image for Java code execution sandbox

set -e

echo "Building Docker image for Java sandbox..."

cd "$(dirname "$0")"

# Build the image
docker build -t proginator-java-sandbox -f src/main/docker/Dockerfile .

echo "Docker image built successfully: proginator-java-sandbox"
echo "You can now run the application with code execution support."