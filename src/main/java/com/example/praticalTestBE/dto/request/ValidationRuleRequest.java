package com.example.praticalTestBE.dto.request;

import com.example.praticalTestBE.domain.enums.ValidationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for defining dynamic validation constraints on a component.
 */
public record ValidationRuleRequest(
    @NotNull(message = "Validation type is mandatory")
    ValidationType validationType,

    String ruleValue,

    @Size(max = 255, message = "Error message cannot exceed 255 characters")
    String errorMessage
) {}
