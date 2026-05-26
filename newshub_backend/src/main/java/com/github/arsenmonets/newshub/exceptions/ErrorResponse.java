package com.github.arsenmonets.newshub.exceptions;

import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        int status,
        LocalDateTime timestamp) {
}
