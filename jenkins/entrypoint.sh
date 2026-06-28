#!/bin/bash
set -e

# Local dev only: allow the jenkins user to talk to the host Podman socket.
if [ -S /var/run/podman.sock ]; then
  chmod 666 /var/run/podman.sock
fi

chown -R jenkins:jenkins /var/jenkins_home

exec gosu jenkins /usr/local/bin/jenkins.sh "$@"
