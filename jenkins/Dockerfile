FROM jenkins/jenkins:lts

USER root

RUN apt-get update && \
    apt-get install -y openjdk-21-jdk podman git gosu && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV CONTAINER_HOST=unix:///var/run/podman.sock

COPY entrypoint.sh /usr/local/bin/jenkins-entrypoint.sh
RUN sed -i 's/\r$//' /usr/local/bin/jenkins-entrypoint.sh && \
    chmod +x /usr/local/bin/jenkins-entrypoint.sh

ENTRYPOINT ["/usr/local/bin/jenkins-entrypoint.sh"]
