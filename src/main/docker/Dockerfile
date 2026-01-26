# Java execution sandbox for secure code execution
FROM debian:bullseye-slim

# Install OpenJDK 17 and necessary tools
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-17-jdk-headless \
    bash \
    coreutils \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN useradd -m -s /bin/bash runner

# Set working directory
WORKDIR /tmp

# Switch to non-root user
USER runner

# Default command to keep container running
CMD ["tail", "-f", "/dev/null"]