package com.example.praticalTestBE.dto.response;

import com.example.praticalTestBE.domain.enums.ComponentType;

import java.util.List;

/**
 * Response payload representing a dynamic field component.
 */
public record ComponentResponse(
    Long id,
    String fieldCode,
    String label,
    ComponentType componentType,
    String placeholder,
    String defaultValue,
    Integer sortOrder,
    Boolean required,
    Boolean visible,
    List<ComponentOptionResponse> options,
    List<ValidationRuleResponse> validationRules,
    List<ComponentResponse> childComponents
) {}
