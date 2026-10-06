package com.example.praticalTestBE.dto.response;

import java.time.LocalDateTime;

/**
 * Standard API response envelope wrapper for successful backend responses.
 */
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success(T data) {
        return success("Success", data);
    }
}
