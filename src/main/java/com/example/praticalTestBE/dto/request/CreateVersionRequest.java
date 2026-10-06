package com.example.praticalTestBE.dto.request;

import jakarta.validation.Valid;

import java.util.List;

/**
 * Request payload for creating a new form version schema draft.
 */
public record CreateVersionRequest(
    @Valid
    List<FormSectionRequest> sections,

    @Valid
    List<ConditionRequest> conditions
) {
    public CreateVersionRequest {
        if (sections == null) sections = List.of();
        if (conditions == null) conditions = List.of();
    }
}
