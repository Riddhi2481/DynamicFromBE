package com.example.praticalTestBE.service;

import com.example.praticalTestBE.domain.entity.FormComponent;
import com.example.praticalTestBE.domain.entity.FormValidationRule;
import com.example.praticalTestBE.domain.enums.ComponentType;
import com.example.praticalTestBE.dto.response.ApiErrorResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Component dynamic validation service evaluating submitted field values against schema rules.
 */
@Service
public class FormValidationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    /**
     * Validates a single component response value against its configured validation rules.
     */
    public List<ApiErrorResponse.FieldErrorDetail> validateComponentValue(FormComponent component, Object rawValue, boolean isDynamicallyRequired) {
        List<ApiErrorResponse.FieldErrorDetail> errors = new ArrayList<>();
        String fieldCode = component.getFieldCode();
        String fieldLabel = component.getLabel();
        String stringVal = rawValue != null ? rawValue.toString().trim() : "";

        // 1. Required field check (Static configuration OR dynamically required via conditional logic)
        boolean required = component.isRequired() || isDynamicallyRequired;
        if (required && (rawValue == null || stringVal.isEmpty())) {
            errors.add(new ApiErrorResponse.FieldErrorDetail(
                fieldCode,
                String.format("'%s' is required", fieldLabel),
                rawValue
            ));
            return errors; // Stop further validation on empty required field
        }

        // If field is optional and empty, skip further value validations
        if (rawValue == null || stringVal.isEmpty()) {
            return errors;
        }

        // 2. Component Type specific basic structure validation
        if (component.getComponentType() == ComponentType.NUMBER) {
            try {
                new BigDecimal(stringVal);
            } catch (NumberFormatException e) {
                errors.add(new ApiErrorResponse.FieldErrorDetail(
                    fieldCode,
                    String.format("'%s' must be a valid number", fieldLabel),
                    rawValue
                ));
            }
        } else if (component.getComponentType() == ComponentType.DATE) {
            try {
                LocalDate.parse(stringVal);
            } catch (DateTimeParseException e) {
                errors.add(new ApiErrorResponse.FieldErrorDetail(
                    fieldCode,
                    String.format("'%s' must be a valid ISO date (YYYY-MM-DD)", fieldLabel),
                    rawValue
                ));
            }
        }

        // 3. Dynamic Validation Rules configured on Component
        if (component.getValidationRules() != null) {
            for (FormValidationRule rule : component.getValidationRules()) {
                String customMsg = rule.getErrorMessage();

                switch (rule.getValidationType()) {
                    case REQUIRED -> {
                        if (stringVal.isEmpty()) {
                            errors.add(new ApiErrorResponse.FieldErrorDetail(
                                fieldCode,
                                customMsg != null ? customMsg : String.format("'%s' is required", fieldLabel),
                                rawValue
                            ));
                        }
                    }
                    case MIN_LENGTH -> {
                        try {
                            int minLen = Integer.parseInt(rule.getRuleValue());
                            if (stringVal.length() < minLen) {
                                errors.add(new ApiErrorResponse.FieldErrorDetail(
                                    fieldCode,
                                    customMsg != null ? customMsg : String.format("'%s' must be at least %d characters", fieldLabel, minLen),
                                    rawValue
                                ));
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                    case MAX_LENGTH -> {
                        try {
                            int maxLen = Integer.parseInt(rule.getRuleValue());
                            if (stringVal.length() > maxLen) {
                                errors.add(new ApiErrorResponse.FieldErrorDetail(
                                    fieldCode,
                                    customMsg != null ? customMsg : String.format("'%s' cannot exceed %d characters", fieldLabel, maxLen),
                                    rawValue
                                ));
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                    case MIN_VALUE -> {
                        try {
                            BigDecimal minVal = new BigDecimal(rule.getRuleValue());
                            BigDecimal submittedVal = new BigDecimal(stringVal);
                            if (submittedVal.compareTo(minVal) < 0) {
                                errors.add(new ApiErrorResponse.FieldErrorDetail(
                                    fieldCode,
                                    customMsg != null ? customMsg : String.format("'%s' must be at least %s", fieldLabel, rule.getRuleValue()),
                                    rawValue
                                ));
                            }
                        } catch (Exception ignored) {}
                    }
                    case MAX_VALUE -> {
                        try {
                            BigDecimal maxVal = new BigDecimal(rule.getRuleValue());
                            BigDecimal submittedVal = new BigDecimal(stringVal);
                            if (submittedVal.compareTo(maxVal) > 0) {
                                errors.add(new ApiErrorResponse.FieldErrorDetail(
                                    fieldCode,
                                    customMsg != null ? customMsg : String.format("'%s' cannot exceed %s", fieldLabel, rule.getRuleValue()),
                                    rawValue
                                ));
                            }
                        } catch (Exception ignored) {}
                    }
                    case REGEX -> {
                        if (rule.getRuleValue() != null && !rule.getRuleValue().isBlank()) {
                            try {
                                Pattern pattern = Pattern.compile(rule.getRuleValue());
                                if (!pattern.matcher(stringVal).matches()) {
                                    errors.add(new ApiErrorResponse.FieldErrorDetail(
                                        fieldCode,
                                        customMsg != null ? customMsg : String.format("'%s' value is invalid", fieldLabel),
                                        rawValue
                                    ));
                                }
                            } catch (PatternSyntaxException ignored) {}
                        }
                    }
                    case CUSTOM -> {
                        // Support custom email / file validations stored in ruleValue
                        if ("EMAIL".equalsIgnoreCase(rule.getRuleValue())) {
                            if (!EMAIL_PATTERN.matcher(stringVal).matches()) {
                                errors.add(new ApiErrorResponse.FieldErrorDetail(
                                    fieldCode,
                                    customMsg != null ? customMsg : String.format("'%s' must be a valid email address", fieldLabel),
                                    rawValue
                                ));
                            }
                        }
                    }
                }
            }
        }

        return errors;
    }
}
