package com.example.praticalTestBE.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Production-quality API error response envelope.
 */
public record ApiErrorResponse(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<FieldErrorDetail> fieldErrors
) {
    public record FieldErrorDetail(
        String field,
        String message,
        Object rejectedValue
    ) {}

    public static ApiErrorResponse of(int status, String error, String message, String path, List<FieldErrorDetail> fieldErrors) {
        return new ApiErrorResponse(LocalDateTime.now(), status, error, message, path, fieldErrors != null ? fieldErrors : List.of());
    }

    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return of(status, error, message, path, List.of());
    }
}
