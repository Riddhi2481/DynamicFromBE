package com.example.praticalTestBE.exception;

import com.example.praticalTestBE.dto.response.ApiErrorResponse;

import java.util.List;

public class ValidationException extends RuntimeException {
    private final List<ApiErrorResponse.FieldErrorDetail> fieldErrors;

    public ValidationException(String message) {
        super(message);
        this.fieldErrors = List.of();
    }

    public ValidationException(String message, List<ApiErrorResponse.FieldErrorDetail> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors != null ? fieldErrors : List.of();
    }

    public List<ApiErrorResponse.FieldErrorDetail> getFieldErrors() {
        return fieldErrors;
    }
}
