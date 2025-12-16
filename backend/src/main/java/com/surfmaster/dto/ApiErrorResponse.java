package com.surfmaster.dto;

import java.time.OffsetDateTime;

/**
 * Represents a standardized error response sent to API consumers.
 *
 * @param message user-friendly description of the problem
 * @param details optional technical details for support
 * @param timestamp instant when the error was generated
 */
public record ApiErrorResponse(
        String message,
        String details,
        OffsetDateTime timestamp
) {
    public static ApiErrorResponse of(String message, String details) {
        return new ApiErrorResponse(message, details, OffsetDateTime.now());
    }
}
