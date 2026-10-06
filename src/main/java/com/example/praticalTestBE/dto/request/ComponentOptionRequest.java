package com.example.praticalTestBE.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for configuring a single selection option (SELECT, RADIO, CHECKBOX).
 */
public record ComponentOptionRequest(
    @NotBlank(message = "Option label is mandatory")
    @Size(max = 255, message = "Option label cannot exceed 255 characters")
    String optionLabel,

    @NotBlank(message = "Option value is mandatory")
    @Size(max = 255, message = "Option value cannot exceed 255 characters")
    String optionValue,

    @NotNull(message = "Sort order is mandatory")
    Integer sortOrder,

    Boolean isDefault
) {
    public ComponentOptionRequest {
        if (isDefault == null) {
            isDefault = false;
        }
    }
}
