package com.example.praticalTestBE.dto.request;

import com.example.praticalTestBE.domain.enums.ComponentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request payload for creating/updating a dynamic form field component.
 * Supports nested child components (e.g. repeaters / field groups).
 */
public record ComponentRequest(
    @NotBlank(message = "Field code is mandatory")
    @Size(max = 100, message = "Field code cannot exceed 100 characters")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Field code must be alphanumeric with underscores")
    String fieldCode,

    @NotBlank(message = "Label is mandatory")
    @Size(max = 255, message = "Label cannot exceed 255 characters")
    String label,

    @NotNull(message = "Component type is mandatory")
    ComponentType componentType,

    @Size(max = 255, message = "Placeholder cannot exceed 255 characters")
    String placeholder,

    String defaultValue,

    @NotNull(message = "Sort order is mandatory")
    Integer sortOrder,

    Boolean required,

    Boolean visible,

    @Valid
    List<ComponentOptionRequest> options,

    @Valid
    List<ValidationRuleRequest> validationRules,

    @Valid
    List<ComponentRequest> childComponents
) {
    public ComponentRequest {
        if (required == null) required = false;
        if (visible == null) visible = true;
        if (options == null) options = List.of();
        if (validationRules == null) validationRules = List.of();
        if (childComponents == null) childComponents = List.of();
    }
}
