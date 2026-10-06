package com.example.praticalTestBE.controller;

import com.example.praticalTestBE.dto.request.SubmissionRequest;
import com.example.praticalTestBE.dto.response.ApiResponse;
import com.example.praticalTestBE.dto.response.SubmissionResponse;
import com.example.praticalTestBE.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Form Submission operations.
 */
@RestController
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    /**
     * POST /api/forms/{formCode}/submissions
     * Purpose: Submit user form response payload against active published form version.
     */
    @PostMapping("/api/forms/{formCode}/submissions")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitForm(
        @PathVariable("formCode") String formCode,
        @Valid @RequestBody SubmissionRequest request
    ) {
        SubmissionResponse submissionResponse = submissionService.submitForm(formCode, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Form submitted successfully", submissionResponse));
    }

    /**
     * GET /api/forms/{formCode}/submissions
     * Purpose: Retrieve paginated submissions for a specific form code.
     */
    @GetMapping("/api/forms/{formCode}/submissions")
    public ResponseEntity<ApiResponse<Page<SubmissionResponse>>> getSubmissionsByFormCode(
        @PathVariable("formCode") String formCode,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Page<SubmissionResponse> submissions = submissionService.getSubmissionsByFormCode(formCode, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Submissions retrieved successfully", submissions));
    }

    /**
     * GET /api/submissions/{submissionId}
     * Purpose: Retrieve submission details and submitted values by submission ID.
     */
    @GetMapping("/api/submissions/{submissionId}")
    public ResponseEntity<ApiResponse<SubmissionResponse>> getSubmissionById(@PathVariable("submissionId") Long submissionId) {
        SubmissionResponse submission = submissionService.getSubmissionById(submissionId);
        return ResponseEntity.ok(ApiResponse.success("Submission detail retrieved successfully", submission));
    }
}
