package com.example.praticalTestBE.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response payload representing full details of a recorded form submission.
 */
public record SubmissionResponse(
    Long id,
    String submissionCode,
    String formCode,
    Integer versionNumber,
    LocalDateTime submittedAt,
    Map<String, Object> submittedData,
    List<SubmissionValueResponse> values
) {}
