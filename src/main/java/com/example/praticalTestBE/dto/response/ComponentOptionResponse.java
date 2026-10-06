package com.example.praticalTestBE.dto.response;

/**
 * Response payload representing a dynamic field component option.
 */
public record ComponentOptionResponse(
    Long id,
    String optionLabel,
    String optionValue,
    Integer sortOrder,
    Boolean isDefault
) {}
