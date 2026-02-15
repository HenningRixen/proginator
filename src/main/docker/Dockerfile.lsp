FROM eclipse-temurin:21-jre

ARG JDTLS_VERSION=1.55.0
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

USER root

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    python3 \
    tar \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /opt/jdtls \
    && JDTLS_FILE="$(curl -fsSL "https://download.eclipse.org/jdtls/milestones/${JDTLS_VERSION}/latest.txt")" \
    && curl -fsSL "https://download.eclipse.org/jdtls/milestones/${JDTLS_VERSION}/${JDTLS_FILE}" -o /tmp/jdtls.tar.gz \
    && tar -xzf /tmp/jdtls.tar.gz -C /opt/jdtls \
    && rm /tmp/jdtls.tar.gz \
    && if [ -x /opt/jdtls/bin/jdtls ]; then ln -sf /opt/jdtls/bin/jdtls /usr/local/bin/jdtls; else ln -sf /opt/jdtls/jdtls /usr/local/bin/jdtls; fi

RUN useradd -m -s /bin/sh runner || true
RUN mkdir -p /tmp/workspaces && chown -R runner:runner /tmp/workspaces

USER runner
WORKDIR /tmp

CMD ["tail", "-f", "/dev/null"]
