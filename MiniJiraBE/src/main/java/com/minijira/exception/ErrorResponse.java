package com.minijira.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(Instant timestamp, int status, String error, String message, String path,
                            Map<String, String> details) {
    public static ErrorResponse of(int status, String error, String message, String path, Map<String, String> details) {
        return new ErrorResponse(Instant.now(), status, error, message, path, details);
    }
}
