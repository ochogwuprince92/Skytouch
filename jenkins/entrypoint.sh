#!/bin/bash
set -e

# Local dev only: allow the jenkins user to talk to the host Docker socket.
if [ -S /var/run/docker.sock ]; then
  chmod 666 /var/run/docker.sock
fi

chown -R jenkins:jenkins /var/jenkins_home

exec gosu jenkins /usr/local/bin/jenkins.sh "$@"
