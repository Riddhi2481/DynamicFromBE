package com.example.praticalTestBE.dto.response;

import com.example.praticalTestBE.domain.enums.FormStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload representing full details of a FormVersion including sections and conditional rules.
 */
public record FormVersionResponse(
    Long id,
    Long formId,
    String formCode,
    Integer versionNumber,
    FormStatus status,
    LocalDateTime publishedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<FormSectionResponse> sections,
    List<ConditionResponse> conditions
) {}
