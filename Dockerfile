FROM registry.access.redhat.com/ubi8/openjdk-21:1.20-3.1724181172 as compiler

WORKDIR /work
COPY gradle /work/gradle
COPY src /work/src
COPY build.gradle.kts gradlew settings.gradle.kts /work/

RUN ./gradlew distTar

FROM registry.access.redhat.com/ubi8/openjdk-21:1.20-3.1724181172

USER root

RUN mkdir -p /opt/ssh-server
ENV SSH_PORT 2020
ENV SSH_HOST_KEY /opt/ssh-server/ssh-keys/etc/ssh/ssh_host_ed25519_key
ENV SSH_AUTHORIZED_KEYS /opt/ssh-server/ssh-keys/server/authorized_keys
ENV SSH_USERNAME user

ENV SSH_SERVER_DIR /opt/ssh-server
ENV SSH_ARTIFACT_NAME ssh-server.tar

COPY scripts/run.sh /opt/ssh-server/

RUN chgrp -R 0 /opt/ssh-server && \
    chmod -R g+rwx /opt/ssh-server

USER 185

COPY --from=compiler /work/build/distributions/ssh-server.tar /opt/ssh-server/
