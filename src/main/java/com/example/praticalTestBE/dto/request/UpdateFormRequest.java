package com.example.praticalTestBE.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request payload for updating form metadata and current draft schema.
 */
public record UpdateFormRequest(
    @NotBlank(message = "Title is mandatory")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    String title,

    String description,

    String category,

    @Valid
    List<FormSectionRequest> sections,

    @Valid
    List<ConditionRequest> conditions
) {
    public UpdateFormRequest {
        if (sections == null) sections = List.of();
        if (conditions == null) conditions = List.of();
    }
}
