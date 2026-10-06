package com.example.praticalTestBE.dto.response;

import com.example.praticalTestBE.domain.enums.FormStatus;

import java.time.LocalDateTime;

/**
 * Lightweight version summary for listing version history.
 */
public record VersionHistoryResponse(
    Long versionId,
    Integer versionNumber,
    FormStatus status,
    LocalDateTime publishedAt,
    LocalDateTime createdAt
) {}
