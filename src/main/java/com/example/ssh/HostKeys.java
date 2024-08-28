package com.example.ssh;

import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.session.SessionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.Iterator;
import java.util.List;

public record HostKeys(
        @NotNull Path ed25519
) implements KeyPairProvider {

    static final @NotNull Logger logger = LoggerFactory.getLogger(HostKeys.class);

    static final @NotNull String SSH_HOST_ED25519_KEY = "ssh_host_ed25519_key";
    static final @NotNull String SSH_HOST_ED25519_KEY_PUB = "ssh_host_ed25519_key.pub";

    static final @NotNull Path DEFAULT_SSH_HOST_KEY_PATH = Path.of("/etc", "ssh", SSH_HOST_ED25519_KEY);

    public static @Nullable HostKeys get(@NotNull String hostKeyEnvName) {
        Path byEnv = Env.getPath(hostKeyEnvName);
        if (byEnv != null && Files.exists(byEnv)) {
            logger.debug("[host-key] using {} as host key", byEnv);
            return new HostKeys(byEnv);
        }
        if (Files.exists(DEFAULT_SSH_HOST_KEY_PATH)) {
            logger.debug("[host-key] using {} as host key", DEFAULT_SSH_HOST_KEY_PATH);
            return new HostKeys(DEFAULT_SSH_HOST_KEY_PATH);
        }
        logger.warn("[host-key] no host key found");
        return null;
    }

    @Override
    public @NotNull Iterable<KeyPair> loadKeys(SessionContext session) {
        FileKeyPairProvider fileKeyPairProvider = new FileKeyPairProvider(List.of(ed25519));
        Iterable<KeyPair> keyPairs = fileKeyPairProvider.loadKeys(session);
        return () -> new Iterator<>() {
            final Iterator<KeyPair> iterator = keyPairs.iterator();
            @Override
            public boolean hasNext() {
                boolean hasNext = iterator.hasNext();
                logger.debug("checking(hasNext={}) host key[ed25519: {}] for session: {}, user: {}, kex: {}, proposal: {}", hasNext, ed25519, session.getSessionId(), session.getUsername(), session.getKexState(), session.getKexNegotiationResult().keySet());
                return hasNext;
            }

            @Override
            public KeyPair next() {
                logger.debug("using host key[ed25519: {}] for session: {}, user: {}, kex: {}, proposal: {}", ed25519, session.getSessionId(), session.getUsername(), session.getKexState(), session.getKexNegotiationResult().keySet());
                return iterator.next();
            }
        };
    }
}
