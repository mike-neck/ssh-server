package com.example.ssh;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.jetbrains.annotations.NotNull;

public class PubKey {

    final @NotNull String username;
    final @NotNull AuthorizedKeyEntry entry;

    public PubKey(@NotNull String username, @NotNull AuthorizedKeyEntry entry) {
        this.username = username;
        this.entry = entry;
    }

    public boolean isSameKey(@NotNull ServerSession session, @NotNull PublicKey publicKey) {
        try {
            PublicKey pubKey = entry.resolvePublicKey(session, entry.getLoginOptions(), null);
            return KeyUtils.compareKeys(pubKey, publicKey);
        } catch (IOException | GeneralSecurityException e) {
            return false;
        }
    }

    public static @NotNull Collection collection(@NotNull String usernameEnvName, @NotNull String authorizedKeysEnvName) {
        Path sshAuthorizedKeys = Env.notNullPath(authorizedKeysEnvName, Path.of("home", "user", ".ssh", "authorized_keys"));
        String username = Env.notNullString(usernameEnvName, "username");
        try {
            List<AuthorizedKeyEntry> entries = AuthorizedKeyEntry.readAuthorizedKeys(sshAuthorizedKeys, StandardOpenOption.READ);
            List<PubKey> pubKeys = new ArrayList<>();
            for (AuthorizedKeyEntry entry : entries) {
                pubKeys.add(new PubKey(username, entry));
            }
            return new Collection(List.copyOf(pubKeys));
        } catch (IOException e) {
            return EMPTY;
        }
    }

    private static final Collection EMPTY = new Collection(List.of());

    public record Collection(@NotNull List<PubKey> pubKeys) implements Iterable<PubKey>, PublickeyAuthenticator {

        @Override
        public @NotNull Iterator<PubKey> iterator() {
            return pubKeys.iterator();
        }

        @Override
        public boolean authenticate(String username, PublicKey publicKey, ServerSession serverSession) throws AsyncAuthException {
            for (PubKey pubKey : this) {
                if (!pubKey.username.equals(username)) {
                    return false;
                }
                if (pubKey.isSameKey(serverSession, publicKey)) {
                    return true;
                }
            }
            return false;
        }
    }
}
