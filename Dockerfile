FROM registry.access.redhat.com/ubi8/openjdk-21:1.20-3.1724181172 as compiler

WORKDIR /work
COPY gradle /work/gradle
COPY src /work/src
COPY build.gradle.kts gradlew settings.gradle.kts /work/

RUN ./gradlew distTar

FROM registry.access.redhat.com/ubi8/openjdk-21:1.20-3.1724181172

RUN mkdir -p /opt/ssh-server && \
    chgrp -R 0 /opt/ssh-server && \
    chmod -R g+rwx /opt/ssh-server
ENV SSH_PORT 2020
ENV SSH_HOST_KEY /opt/ssh-server/etc/ssh/ssh_host_ed25519_key
ENV SSH_AUTHORIZED_KEYS /opt/ssh-server/authorized_keys
ENV SSH_USERNAME user

COPY --from=compiler /work/build/distributions/ssh-server-1.0-SNAPSHOT.tar /opt/ssh-server/
