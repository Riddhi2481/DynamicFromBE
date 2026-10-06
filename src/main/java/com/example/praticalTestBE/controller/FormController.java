package com.example.praticalTestBE.controller;

import com.example.praticalTestBE.dto.request.CreateFormRequest;
import com.example.praticalTestBE.dto.request.UpdateFormRequest;
import com.example.praticalTestBE.dto.response.ApiResponse;
import com.example.praticalTestBE.dto.response.FormListResponse;
import com.example.praticalTestBE.dto.response.FormResponse;
import com.example.praticalTestBE.dto.response.PublishedFormResponse;
import com.example.praticalTestBE.service.FormService;
import com.example.praticalTestBE.service.FormVersionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Form Aggregate CRUD operations and published schema retrieval.
 */
@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
public class FormController {

    private final FormService formService;
    private final FormVersionService versionService;

    /**
     * POST /api/forms
     * Purpose: Create a new Form and initialize Version 1 in DRAFT state.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FormResponse>> createForm(@Valid @RequestBody CreateFormRequest request) {
        FormResponse createdForm = formService.createForm(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Form created successfully", createdForm));
    }

    /**
     * GET /api/forms
     * Purpose: List all form summaries.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<FormListResponse>> listForms() {
        FormListResponse forms = formService.listForms();
        return ResponseEntity.ok(ApiResponse.success("Forms retrieved successfully", forms));
    }

    /**
     * GET /api/forms/{id}
     * Purpose: Fetch details of a specific form by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FormResponse>> getFormById(@PathVariable("id") Long id) {
        FormResponse form = formService.getFormById(id);
        return ResponseEntity.ok(ApiResponse.success("Form details retrieved successfully", form));
    }

    /**
     * PUT /api/forms/{id}
     * Purpose: Update form metadata and current draft version schema.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FormResponse>> updateForm(
        @PathVariable("id") Long id,
        @Valid @RequestBody UpdateFormRequest request
    ) {
        FormResponse updatedForm = formService.updateForm(id, request);
        return ResponseEntity.ok(ApiResponse.success("Form updated successfully", updatedForm));
    }

    /**
     * DELETE /api/forms/{id}
     * Purpose: Delete a form and associated versions.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteForm(@PathVariable("id") Long id) {
        formService.deleteForm(id);
        return ResponseEntity.ok(ApiResponse.success("Form deleted successfully", null));
    }

    /**
     * GET /api/forms/{formCode}/published
     * Purpose: Public endpoint retrieving active published schema for UI rendering.
     */
    @GetMapping("/{formCode}/published")
    public ResponseEntity<ApiResponse<PublishedFormResponse>> getPublishedSchema(@PathVariable("formCode") String formCode) {
        PublishedFormResponse publishedSchema = versionService.getPublishedSchema(formCode);
        return ResponseEntity.ok(ApiResponse.success("Published form schema retrieved successfully", publishedSchema));
    }
}
