package com.example.praticalTestBE.dto.response;

import java.util.List;

/**
 * Response payload representing a layout section container with ordered components.
 */
public record FormSectionResponse(
    Long id,
    String sectionCode,
    String title,
    String description,
    Integer sortOrder,
    List<ComponentResponse> components
) {}
