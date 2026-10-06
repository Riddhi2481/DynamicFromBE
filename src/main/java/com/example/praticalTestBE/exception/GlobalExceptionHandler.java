package com.example.praticalTestBE.exception;

import com.example.praticalTestBE.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Production-quality Global Exception Handler for the Dynamic Form Builder.
 * Maps application exceptions to standardized HTTP status codes and error responses without exposing stack traces.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 404 NOT FOUND - Resource not found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.of(
            HttpStatus.NOT_FOUND.value(),
            "NOT_FOUND",
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * 409 CONFLICT - Duplicate form code or resource conflict.
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateResource(DuplicateResourceException ex, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.of(
            HttpStatus.CONFLICT.value(),
            "DUPLICATE_RESOURCE",
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * 400 BAD REQUEST / 422 UNPROCESSABLE ENTITY - Submission validation failures.
     */
    @ExceptionHandler(SubmissionValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleSubmissionValidation(SubmissionValidationException ex, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.of(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "SUBMISSION_VALIDATION_ERROR",
            ex.getMessage(),
            request.getRequestURI(),
            ex.getFieldErrors()
        );
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(FormValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleFormValidation(FormValidationException ex, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.of(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "SUBMISSION_VALIDATION_ERROR",
            ex.getMessage(),
            request.getRequestURI(),
            ex.getFieldErrors()
        );
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(ValidationException ex, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_ERROR",
            ex.getMessage(),
            request.getRequestURI(),
            ex.getFieldErrors()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * 400 BAD REQUEST - Form configuration & schema errors.
     */
    @ExceptionHandler({InvalidFormConfigurationException.class, InvalidSchemaException.class})
    public ResponseEntity<ApiErrorResponse> handleInvalidFormConfiguration(RuntimeException ex, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "INVALID_FORM_CONFIGURATION",
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * 422 UNPROCESSABLE ENTITY - Invalid condition rules & circular dependencies.
     */
    @ExceptionHandler({InvalidConditionException.class, CircularDependencyException.class})
    public ResponseEntity<ApiErrorResponse> handleInvalidCondition(RuntimeException ex, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.of(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "INVALID_CONDITION",
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * 400 BAD REQUEST - Versioning & lifecycle state errors.
     */
    @ExceptionHandler({InvalidVersionException.class, IllegalFormStateException.class})
    public ResponseEntity<ApiErrorResponse> handleInvalidVersion(RuntimeException ex, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "INVALID_VERSION",
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * 422 UNPROCESSABLE ENTITY - Form publishing failures.
     */
    @ExceptionHandler(FormPublishingException.class)
    public ResponseEntity<ApiErrorResponse> handleFormPublishing(FormPublishingException ex, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.of(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "PUBLISHING_ERROR",
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(FormNotPublishedException.class)
    public ResponseEntity<ApiErrorResponse> handleFormNotPublished(FormNotPublishedException ex, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "FORM_NOT_PUBLISHED",
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * 400 BAD REQUEST - Jakarta validation failures on request DTOs.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiErrorResponse.FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(err -> new ApiErrorResponse.FieldErrorDetail(
                err.getField(),
                err.getDefaultMessage(),
                err.getRejectedValue()
            ))
            .toList();

        ApiErrorResponse error = ApiErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_ERROR",
            "Request validation failed for input parameters",
            request.getRequestURI(),
            fieldErrors
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * 400 BAD REQUEST - Malformed JSON body or invalid Enum values.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String msg = "Malformed JSON request payload or invalid enum constant value";
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            msg += ": " + ex.getCause().getMessage();
        }
        ApiErrorResponse error = ApiErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "MALFORMED_JSON",
            msg,
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * 409 CONFLICT - Database constraint violations (e.g. unique key violation).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.of(
            HttpStatus.CONFLICT.value(),
            "DATABASE_CONSTRAINT_VIOLATION",
            "Database constraint violation: duplicate entry or invalid reference",
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * 500 INTERNAL SERVER ERROR - Uncaught runtime exceptions.
     * Prevents stack trace leakage to API clients.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "INTERNAL_SERVER_ERROR",
            "An unexpected internal error occurred. Please contact the administrator.",
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
