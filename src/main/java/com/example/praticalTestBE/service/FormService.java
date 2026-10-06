package com.example.praticalTestBE.service;

import com.example.praticalTestBE.domain.entity.*;
import com.example.praticalTestBE.domain.enums.AuditAction;
import com.example.praticalTestBE.domain.enums.FormStatus;
import com.example.praticalTestBE.dto.request.CreateFormRequest;
import com.example.praticalTestBE.dto.request.UpdateFormRequest;
import com.example.praticalTestBE.dto.response.*;
import com.example.praticalTestBE.exception.DuplicateResourceException;
import com.example.praticalTestBE.exception.ResourceNotFoundException;
import com.example.praticalTestBE.repository.FormRepository;
import com.example.praticalTestBE.repository.FormVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service orchestrating top-level Form aggregate lifecycle operations.
 */
@Service
@RequiredArgsConstructor
public class FormService {

    private final FormRepository formRepository;
    private final FormVersionRepository versionRepository;
    private final FormVersionService versionService;
    private final AuditService auditService;

    @Transactional
    public FormResponse createForm(CreateFormRequest request) {
        if (formRepository.existsByFormCode(request.formCode())) {
            throw new DuplicateResourceException("Form already exists with formCode: " + request.formCode());
        }

        Form form = Form.builder()
            .formCode(request.formCode())
            .title(request.title())
            .description(request.description())
            .category(request.category())
            .build();

        Form savedForm = formRepository.save(form);

        // Initialize Version 1 as DRAFT
        FormVersion v1 = FormVersion.builder()
            .form(savedForm)
            .versionNumber(1)
            .status(FormStatus.DRAFT)
            .build();

        if (request.initialSections() != null && !request.initialSections().isEmpty()) {
            versionService.buildSchemaFromRequests(v1, request.initialSections(), request.initialConditions());
        }

        savedForm.addVersion(v1);
        FormVersion savedV1 = versionRepository.save(v1);

        auditService.logEvent(
            savedForm.getId(),
            savedV1.getId(),
            savedForm.getFormCode(),
            AuditAction.FORM_CREATED,
            "SYSTEM",
            "Form created with initial version 1 DRAFT"
        );

        return mapToFormResponse(savedForm);
    }

    @Transactional
    public FormResponse updateForm(Long id, UpdateFormRequest request) {
        Form form = formRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Form not found with id: " + id));

        form.setTitle(request.title());
        if (request.description() != null) {
            form.setDescription(request.description());
        }
        if (request.category() != null) {
            form.setCategory(request.category());
        }

        // Get or create draft for edit (if form was published, automatically forks a new draft version)
        FormVersion draft = versionService.getOrCreateDraftForEdit(form);
        if (request.sections() != null) {
            versionService.buildSchemaFromRequests(draft, request.sections(), request.conditions());
        }

        Form savedForm = formRepository.save(form);
        auditService.logEvent(
            savedForm.getId(),
            draft.getId(),
            savedForm.getFormCode(),
            AuditAction.FORM_UPDATED,
            "SYSTEM",
            "Updated form metadata and draft schema for version " + draft.getVersionNumber()
        );

        return mapToFormResponse(savedForm);
    }

    @Transactional(readOnly = true)
    public FormResponse getFormById(Long id) {
        Form form = formRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Form not found with id: " + id));
        return mapToFormResponse(form);
    }

    @Transactional(readOnly = true)
    public FormResponse getFormByCode(String formCode) {
        Form form = formRepository.findByFormCode(formCode)
            .orElseThrow(() -> new ResourceNotFoundException("Form not found with formCode: " + formCode));
        return mapToFormResponse(form);
    }

    @Transactional(readOnly = true)
    public FormListResponse listForms() {
        List<Form> forms = formRepository.findAllByOrderByUpdatedAtDesc();
        List<FormResponse> formResponses = forms.stream()
            .map(this::mapToFormResponse)
            .toList();
        return new FormListResponse(formResponses, formResponses.size(), 1);
    }

    @Transactional
    public void deleteForm(Long id) {
        Form form = formRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Form not found with id: " + id));

        formRepository.delete(form);

        auditService.logEvent(
            id,
            null,
            form.getFormCode(),
            AuditAction.FORM_DELETED,
            "SYSTEM",
            "Form and associated versions deleted"
        );
    }

    @Transactional
    public FormResponse cloneForm(Long id, String newFormCode, String newTitle) {
        Form sourceForm = formRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Source form not found with id: " + id));

        if (formRepository.existsByFormCode(newFormCode)) {
            throw new DuplicateResourceException("Form already exists with formCode: " + newFormCode);
        }

        CreateFormRequest createReq = new CreateFormRequest(
            newFormCode,
            newTitle != null ? newTitle : "Copy of " + sourceForm.getTitle(),
            sourceForm.getDescription(),
            sourceForm.getCategory(),
            List.of(),
            List.of()
        );

        FormResponse clonedResponse = createForm(createReq);
        Form clonedForm = formRepository.findById(clonedResponse.id()).orElseThrow();

        // Find latest version from source form to copy schema
        FormVersion latestSourceVer = versionRepository.findByFormIdOrderByVersionNumberDesc(id)
            .stream().findFirst().orElse(null);

        if (latestSourceVer != null) {
            FormVersion targetDraft = versionRepository.findByFormIdAndStatus(clonedForm.getId(), FormStatus.DRAFT).orElseThrow();
            FormVersionResponse srcVerRes = versionService.mapToVersionResponse(latestSourceVer);

            // Convert responses back to requests for schema building
            List<com.example.praticalTestBE.dto.request.FormSectionRequest> secReqs = srcVerRes.sections() != null ?
                srcVerRes.sections().stream().map(s -> new com.example.praticalTestBE.dto.request.FormSectionRequest(
                    s.sectionCode(), s.title(), s.description(), s.sortOrder(),
                    s.components() != null ? s.components().stream().map(this::mapCompToReq).toList() : List.of()
                )).toList() : List.of();

            List<com.example.praticalTestBE.dto.request.ConditionRequest> condReqs = srcVerRes.conditions() != null ?
                srcVerRes.conditions().stream().map(c -> new com.example.praticalTestBE.dto.request.ConditionRequest(
                    c.triggerFieldCode(), c.operator(), c.logicOperator(), c.triggerValue(), c.targetFieldCode(), c.action(), c.actionValue()
                )).toList() : List.of();

            versionService.buildSchemaFromRequests(targetDraft, secReqs, condReqs);
            versionRepository.save(targetDraft);
        }

        return mapToFormResponse(clonedForm);
    }

    @Transactional(readOnly = true)
    public PublishedFormResponse previewForm(Long id, Integer versionNumber) {
        if (!formRepository.existsById(id)) {
            throw new ResourceNotFoundException("Form not found with id: " + id);
        }

        FormVersion version;
        if (versionNumber != null) {
            version = versionRepository.findByFormIdAndVersionNumber(id, versionNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Form version not found: " + versionNumber));
        } else {
            // Default to latest version
            version = versionRepository.findByFormIdOrderByVersionNumberDesc(id)
                .stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No version found for form: " + id));
        }

        return versionService.mapToPublishedFormResponse(version);
    }

    private FormResponse mapToFormResponse(Form form) {
        List<FormVersion> versions = versionRepository.findByFormIdOrderByVersionNumberDesc(form.getId());

        Integer draftVerNum = versions.stream()
            .filter(v -> v.getStatus() == FormStatus.DRAFT)
            .map(v -> v.getVersionNumber())
            .findFirst().orElse(null);

        Integer pubVerNum = versions.stream()
            .filter(v -> v.getStatus() == FormStatus.PUBLISHED)
            .map(v -> v.getVersionNumber())
            .findFirst().orElse(null);

        return new FormResponse(
            form.getId(),
            form.getFormCode(),
            form.getTitle(),
            form.getDescription(),
            form.getCategory(),
            draftVerNum,
            pubVerNum,
            form.getCreatedAt(),
            form.getUpdatedAt()
        );
    }

    private com.example.praticalTestBE.dto.request.ComponentRequest mapCompToReq(ComponentResponse c) {
        List<com.example.praticalTestBE.dto.request.ComponentOptionRequest> opts = c.options() != null ?
            c.options().stream().map(o -> new com.example.praticalTestBE.dto.request.ComponentOptionRequest(o.optionLabel(), o.optionValue(), o.sortOrder(), o.isDefault())).toList() : List.of();

        List<com.example.praticalTestBE.dto.request.ValidationRuleRequest> rules = c.validationRules() != null ?
            c.validationRules().stream().map(r -> new com.example.praticalTestBE.dto.request.ValidationRuleRequest(r.validationType(), r.ruleValue(), r.errorMessage())).toList() : List.of();

        List<com.example.praticalTestBE.dto.request.ComponentRequest> children = c.childComponents() != null ?
            c.childComponents().stream().map(this::mapCompToReq).toList() : List.of();

        return new com.example.praticalTestBE.dto.request.ComponentRequest(
            c.fieldCode(), c.label(), c.componentType(), c.placeholder(), c.defaultValue(),
            c.sortOrder(), c.required(), c.visible(), opts, rules, children
        );
    }
}
