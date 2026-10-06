package com.example.praticalTestBE.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request payload for creating/updating a layout section container.
 */
public record FormSectionRequest(
    @NotBlank(message = "Section code is mandatory")
    @Size(max = 100, message = "Section code cannot exceed 100 characters")
    String sectionCode,

    @NotBlank(message = "Section title is mandatory")
    @Size(max = 255, message = "Section title cannot exceed 255 characters")
    String title,

    String description,

    @NotNull(message = "Sort order is mandatory")
    Integer sortOrder,

    @Valid
    List<ComponentRequest> components
) {
    public FormSectionRequest {
        if (components == null) components = List.of();
    }
}
