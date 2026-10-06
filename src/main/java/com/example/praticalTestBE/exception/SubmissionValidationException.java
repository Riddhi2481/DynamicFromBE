package com.example.praticalTestBE.exception;

import com.example.praticalTestBE.dto.response.ApiErrorResponse;

import java.util.List;

public class SubmissionValidationException extends RuntimeException {
    private final List<ApiErrorResponse.FieldErrorDetail> fieldErrors;

    public SubmissionValidationException(String message) {
        super(message);
        this.fieldErrors = List.of();
    }

    public SubmissionValidationException(String message, List<ApiErrorResponse.FieldErrorDetail> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors != null ? fieldErrors : List.of();
    }

    public List<ApiErrorResponse.FieldErrorDetail> getFieldErrors() {
        return fieldErrors;
    }
}
