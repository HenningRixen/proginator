# Java execution sandbox for secure code execution
FROM debian:bullseye-slim

# Install OpenJDK 17, JUnit 5, and necessary tools
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-17-jdk-headless \
    bash \
    coreutils \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Download JUnit Platform Console Standalone (includes JUnit Jupiter)
RUN wget -q -O /opt/junit-platform-console-standalone.jar \
    https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.0/junit-platform-console-standalone-1.10.0.jar

# Create non-root user for security
RUN useradd -m -s /bin/bash runner

# Set working directory
WORKDIR /tmp

# Switch to non-root user
USER runner

# Default command to keep container running
CMD ["tail", "-f", "/dev/null"]