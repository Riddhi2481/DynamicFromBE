package com.example.praticalTestBE.dto.response;

import java.util.List;

/**
 * Paginated or full list response wrapper for forms.
 */
public record FormListResponse(
    List<FormResponse> forms,
    long totalElements,
    int totalPages
) {}
