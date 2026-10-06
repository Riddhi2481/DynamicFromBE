package com.example.praticalTestBE.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload representing an explicit field code value entry.
 */
public record SubmissionValueRequest(
    @NotBlank(message = "Field code is mandatory")
    @Size(max = 100)
    String fieldCode,

    String value,

    String valueJson
) {}
