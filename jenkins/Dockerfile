FROM jenkins/jenkins:lts

USER root

RUN apt-get update && \
    apt-get install -y git gosu && \
    rm -rf /var/lib/apt/lists/*

COPY entrypoint.sh /usr/local/bin/jenkins-entrypoint.sh

RUN sed -i 's/\r$//' /usr/local/bin/jenkins-entrypoint.sh && \
    chmod +x /usr/local/bin/jenkins-entrypoint.sh

ENTRYPOINT ["/usr/local/bin/jenkins-entrypoint.sh"]