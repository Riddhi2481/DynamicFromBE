package com.example.praticalTestBE.dto.request;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

/**
 * Request payload for submitting form responses.
 * Supports dynamic key-value map inputs (e.g., {"fieldCode": "value"})
 * as well as structured value lists.
 */
public record SubmissionRequest(
    Map<String, Object> data,

    @Valid
    List<SubmissionValueRequest> values
) {
    public SubmissionRequest {
        if (data == null) data = Map.of();
        if (values == null) values = List.of();
    }
}
