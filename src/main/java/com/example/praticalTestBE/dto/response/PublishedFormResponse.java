package com.example.praticalTestBE.dto.response;

import java.util.List;

/**
 * Public response payload containing active published form layout and logic rules for UI rendering.
 */
public record PublishedFormResponse(
    String formCode,
    String title,
    String description,
    Integer versionNumber,
    List<FormSectionResponse> sections,
    List<ConditionResponse> conditions
) {}
