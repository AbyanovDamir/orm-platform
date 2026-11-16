package ru.learning.platform.service;

import java.util.Optional;

public class PlatformException extends RuntimeException {
    public enum Kind { NOT_FOUND, BAD_INPUT, FORBIDDEN, CONFLICT }

    private final Kind kind;

    public PlatformException(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    public static <T> T require(Optional<T> result, String resource, Long id) {
        return result.orElseThrow(() -> new PlatformException(Kind.NOT_FOUND, resource + " №" + id + " отсутствует"));
    }

    public static void check(boolean condition, Kind kind, String message) {
        if (!condition) {
            throw new PlatformException(kind, message);
        }
    }
}
