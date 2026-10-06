package com.example.praticalTestBE.controller;

import com.example.praticalTestBE.domain.entity.FormVersion;
import com.example.praticalTestBE.dto.request.CreateVersionRequest;
import com.example.praticalTestBE.dto.response.ApiResponse;
import com.example.praticalTestBE.dto.response.FormVersionResponse;
import com.example.praticalTestBE.dto.response.VersionHistoryResponse;
import com.example.praticalTestBE.service.FormPublishingService;
import com.example.praticalTestBE.service.FormVersionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Form Versioning and Publishing endpoints.
 */
@RestController
@RequestMapping("/api/forms/{id}/versions")
@RequiredArgsConstructor
public class FormVersionController {

    private final FormVersionService versionService;
    private final FormPublishingService publishingService;

    /**
     * GET /api/forms/{id}/versions
     * Purpose: Retrieve version history for a form.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<VersionHistoryResponse>>> getVersionHistory(@PathVariable("id") Long id) {
        List<VersionHistoryResponse> history = versionService.getVersionHistory(id);
        return ResponseEntity.ok(ApiResponse.success("Version history retrieved successfully", history));
    }

    /**
     * POST /api/forms/{id}/versions
     * Purpose: Create a new draft form version.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FormVersionResponse>> createVersion(
        @PathVariable("id") Long id,
        @Valid @RequestBody(required = false) CreateVersionRequest request
    ) {
        FormVersionResponse createdVersion = versionService.createNewDraftVersion(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Draft version created successfully", createdVersion));
    }

    /**
     * GET /api/forms/{id}/versions/{version}
     * Purpose: Get details and schema of a specific version number.
     */
    @GetMapping("/{version}")
    public ResponseEntity<ApiResponse<FormVersionResponse>> getVersionDetails(
        @PathVariable("id") Long id,
        @PathVariable("version") Integer version
    ) {
        FormVersionResponse versionDetails = versionService.getVersionDetails(id, version);
        return ResponseEntity.ok(ApiResponse.success("Version details retrieved successfully", versionDetails));
    }

    /**
     * POST /api/forms/{id}/versions/{version}/publish
     * Purpose: Publish a specific draft form version.
     */
    @PostMapping("/{version}/publish")
    public ResponseEntity<ApiResponse<FormVersionResponse>> publishVersion(
        @PathVariable("id") Long id,
        @PathVariable("version") Integer version
    ) {
        FormVersion publishedVersion = publishingService.publishVersion(id, version);
        FormVersionResponse response = versionService.mapToVersionResponse(publishedVersion);
        return ResponseEntity.ok(ApiResponse.success("Form version " + version + " published successfully", response));
    }
}
