package com.example.praticalTestBE.dto.response;

import java.time.LocalDateTime;

/**
 * Response payload representing summary details of a Form aggregate.
 */
public record FormResponse(
    Long id,
    String formCode,
    String title,
    String description,
    String category,
    Integer currentDraftVersion,
    Integer publishedVersion,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
