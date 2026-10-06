package com.example.praticalTestBE.dto.response;

/**
 * Response payload representing an individual submitted field entry with historical snapshot labels.
 */
public record SubmissionValueResponse(
    Long id,
    String fieldCode,
    String fieldLabel,
    String value,
    String valueJson
) {}
