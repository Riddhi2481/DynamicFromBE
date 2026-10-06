package com.example.praticalTestBE.service;

import com.example.praticalTestBE.domain.entity.*;
import com.example.praticalTestBE.domain.enums.AuditAction;
import com.example.praticalTestBE.domain.enums.ConditionAction;
import com.example.praticalTestBE.domain.enums.FormStatus;
import com.example.praticalTestBE.dto.request.SubmissionRequest;
import com.example.praticalTestBE.dto.request.SubmissionValueRequest;
import com.example.praticalTestBE.dto.response.ApiErrorResponse;
import com.example.praticalTestBE.dto.response.SubmissionResponse;
import com.example.praticalTestBE.dto.response.SubmissionValueResponse;
import com.example.praticalTestBE.exception.FormNotPublishedException;
import com.example.praticalTestBE.exception.ResourceNotFoundException;
import com.example.praticalTestBE.exception.SubmissionValidationException;
import com.example.praticalTestBE.repository.FormSubmissionRepository;
import com.example.praticalTestBE.repository.FormVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service managing user form submission validation, dynamic condition processing, and storage.
 */
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final FormVersionRepository versionRepository;
    private final FormSubmissionRepository submissionRepository;
    private final ConditionalLogicService conditionalLogicService;
    private final FormValidationService validationService;
    private final AuditService auditService;

    /**
     * Complete submission execution flow:
     * 1. Identify form & published version.
     * 2. Load exact version configuration.
     * 3. Evaluate conditional rules against submitted payload.
     * 4. Perform dynamic component validations (handling dynamically required/hidden fields).
     * 5. Persist submission + snapshot value records.
     * 6. Return response confirmation DTO.
     */
    @Transactional
    public SubmissionResponse submitForm(String formCode, SubmissionRequest request) {
        // 1. Identify active PUBLISHED version
        FormVersion publishedVersion = versionRepository.findPublishedVersionWithSchema(formCode, FormStatus.PUBLISHED)
            .orElseThrow(() -> new FormNotPublishedException("No active PUBLISHED version found for formCode: " + formCode));

        // 2. Build consolidated submission data map
        Map<String, Object> submittedDataMap = new HashMap<>();
        if (request.data() != null) {
            submittedDataMap.putAll(request.data());
        }
        if (request.values() != null) {
            for (SubmissionValueRequest valReq : request.values()) {
                if (valReq.fieldCode() != null) {
                    submittedDataMap.put(valReq.fieldCode(), valReq.value() != null ? valReq.value() : valReq.valueJson());
                }
            }
        }

        // 3. Evaluate conditional rules
        List<FormCondition> conditions = publishedVersion.getConditions();
        Map<String, Set<ConditionAction>> activeActions = conditionalLogicService.evaluateConditions(conditions, submittedDataMap);

        // 4. Validate submitted values against schema components
        List<ApiErrorResponse.FieldErrorDetail> validationErrors = new ArrayList<>();
        List<FormSection> sections = publishedVersion.getSections();

        if (sections != null) {
            for (FormSection section : sections) {
                if (section.getComponents() != null) {
                    for (FormComponent component : section.getComponents()) {
                        String fieldCode = component.getFieldCode();
                        Set<ConditionAction> compActions = activeActions.getOrDefault(fieldCode, Set.of());

                        // Check if field is dynamically hidden by condition rule
                        if (compActions.contains(ConditionAction.HIDE_COMPONENT) || compActions.contains(ConditionAction.DISABLE_COMPONENT)) {
                            continue; // Skip validation for hidden fields
                        }

                        boolean isDynamicallyRequired = compActions.contains(ConditionAction.SET_REQUIRED);
                        boolean isDynamicallyOptional = compActions.contains(ConditionAction.SET_OPTIONAL);

                        boolean effectiveRequired = (component.isRequired() || isDynamicallyRequired) && !isDynamicallyOptional;

                        Object rawVal = submittedDataMap.get(fieldCode);
                        List<ApiErrorResponse.FieldErrorDetail> compErrors = validationService.validateComponentValue(
                            component, rawVal, effectiveRequired
                        );
                        validationErrors.addAll(compErrors);
                    }
                }
            }
        }

        // If validation errors exist, abort transaction and throw SubmissionValidationException
        if (!validationErrors.isEmpty()) {
            throw new SubmissionValidationException("Validation failed for form submission", validationErrors);
        }

        // 5. Generate unique submission code and persist entity
        String submissionCode = "SUB-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        FormSubmission submission = FormSubmission.builder()
            .formVersion(publishedVersion)
            .submissionCode(submissionCode)
            .build();

        if (sections != null) {
            for (FormSection section : sections) {
                if (section.getComponents() != null) {
                    for (FormComponent comp : section.getComponents()) {
                        String fieldCode = comp.getFieldCode();
                        if (submittedDataMap.containsKey(fieldCode)) {
                            Object val = submittedDataMap.get(fieldCode);
                            String valStr = val != null ? val.toString() : null;

                            FormSubmissionValue subValue = FormSubmissionValue.builder()
                                .submission(submission)
                                .component(comp)
                                .fieldCode(fieldCode)
                                .fieldLabel(comp.getLabel())
                                .value(valStr)
                                .build();
                            submission.addValue(subValue);
                        }
                    }
                }
            }
        }

        FormSubmission savedSubmission = submissionRepository.save(submission);

        auditService.logEvent(
            publishedVersion.getForm().getId(),
            publishedVersion.getId(),
            formCode,
            AuditAction.SUBMISSION_CREATED,
            "PUBLIC_USER",
            "Submitted form response code: " + submissionCode
        );

        return mapToSubmissionResponse(savedSubmission, submittedDataMap);
    }

    @Transactional(readOnly = true)
    public Page<SubmissionResponse> getSubmissionsByFormCode(String formCode, Pageable pageable) {
        Page<FormSubmission> page = submissionRepository.findByFormVersionFormFormCodeOrderBySubmittedAtDesc(formCode, pageable);
        return page.map(s -> mapToSubmissionResponse(s, extractSubmittedDataMap(s)));
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionById(Long submissionId) {
        FormSubmission submission = submissionRepository.findByIdWithValues(submissionId)
            .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));
        return mapToSubmissionResponse(submission, extractSubmittedDataMap(submission));
    }

    private SubmissionResponse mapToSubmissionResponse(FormSubmission submission, Map<String, Object> dataMap) {
        List<SubmissionValueResponse> valueResponses = submission.getValues() != null ?
            submission.getValues().stream().map(v -> new SubmissionValueResponse(v.getId(), v.getFieldCode(), v.getFieldLabel(), v.getValue(), v.getValueJson())).toList() : List.of();

        return new SubmissionResponse(
            submission.getId(),
            submission.getSubmissionCode(),
            submission.getFormVersion() != null && submission.getFormVersion().getForm() != null ? submission.getFormVersion().getForm().getFormCode() : null,
            submission.getFormVersion() != null ? submission.getFormVersion().getVersionNumber() : null,
            submission.getSubmittedAt(),
            dataMap,
            valueResponses
        );
    }

    private Map<String, Object> extractSubmittedDataMap(FormSubmission submission) {
        Map<String, Object> map = new HashMap<>();
        if (submission.getValues() != null) {
            for (FormSubmissionValue v : submission.getValues()) {
                map.put(v.getFieldCode(), v.getValue() != null ? v.getValue() : v.getValueJson());
            }
        }
        return map;
    }
}
