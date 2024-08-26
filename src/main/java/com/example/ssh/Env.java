package com.example.ssh;

import java.nio.file.Path;
import java.util.OptionalInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Env {

    static @Nullable String getString(@NotNull String name) {
        return System.getenv(name);
    }

    static @NotNull String notNullString(@NotNull String name, @NotNull String defaultValue) {
        String env = getString(name);
        return env != null ? env : defaultValue;
    }

    static @NotNull OptionalInt getInt(@NotNull String name) {
        String env = getString(name);
        if (env == null) {
            return OptionalInt.empty();
        }
        try {
            int value = Integer.parseInt(env);
            return OptionalInt.of(value);
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    static int notNullInt(@NotNull String name, int defaultValue) {
        OptionalInt env = getInt(name);
        return env.orElse(defaultValue);
    }

    static @Nullable Path getPath(@NotNull String name) {
        String path = getString(name);
        if (path == null || path.isEmpty()) {
            return null;
        }
        return Path.of(path);
    }

    static @NotNull Path notNullPath(@NotNull String name, @NotNull Path defaultValue) {
        Path path = getPath(name);
        if (path == null) {
            return defaultValue;
        }
        return path;
    }
}
