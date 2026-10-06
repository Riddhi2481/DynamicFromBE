package com.example.praticalTestBE.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request payload for creating a new Form aggregate record and its initial version.
 */
public record CreateFormRequest(
    @NotBlank(message = "Form code is mandatory")
    @Size(max = 100, message = "Form code cannot exceed 100 characters")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Form code must be alphanumeric with underscores")
    String formCode,

    @NotBlank(message = "Title is mandatory")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    String title,

    String description,

    String category,

    @Valid
    List<FormSectionRequest> initialSections,

    @Valid
    List<ConditionRequest> initialConditions
) {
    public CreateFormRequest {
        if (initialSections == null) initialSections = List.of();
        if (initialConditions == null) initialConditions = List.of();
    }
}
