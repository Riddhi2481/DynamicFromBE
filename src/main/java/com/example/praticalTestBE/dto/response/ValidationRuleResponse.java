package com.example.praticalTestBE.dto.response;

import com.example.praticalTestBE.domain.enums.ValidationType;

/**
 * Response payload representing a dynamic field component validation rule.
 */
public record ValidationRuleResponse(
    Long id,
    ValidationType validationType,
    String ruleValue,
    String errorMessage
) {}
