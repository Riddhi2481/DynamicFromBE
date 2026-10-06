package com.example.praticalTestBE.exception;

import com.example.praticalTestBE.dto.response.ApiErrorResponse;

import java.util.List;

public class FormValidationException extends RuntimeException {
    private final List<ApiErrorResponse.FieldErrorDetail> fieldErrors;

    public FormValidationException(String message) {
        super(message);
        this.fieldErrors = List.of();
    }

    public FormValidationException(String message, List<ApiErrorResponse.FieldErrorDetail> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors != null ? fieldErrors : List.of();
    }

    public List<ApiErrorResponse.FieldErrorDetail> getFieldErrors() {
        return fieldErrors;
    }
}
