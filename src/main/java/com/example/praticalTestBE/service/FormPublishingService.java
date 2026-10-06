package com.example.praticalTestBE.service;

import com.example.praticalTestBE.domain.entity.*;
import com.example.praticalTestBE.domain.enums.AuditAction;
import com.example.praticalTestBE.domain.enums.ComponentType;
import com.example.praticalTestBE.domain.enums.FormStatus;
import com.example.praticalTestBE.exception.IllegalFormStateException;
import com.example.praticalTestBE.exception.InvalidSchemaException;
import com.example.praticalTestBE.exception.ResourceNotFoundException;
import com.example.praticalTestBE.repository.FormVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service managing form publishing lifecycle transitions and pre-publish schema integrity validation.
 */
@Service
@RequiredArgsConstructor
public class FormPublishingService {

    private final FormVersionRepository versionRepository;
    private final ConditionalLogicService conditionalLogicService;
    private final AuditService auditService;

    /**
     * Publishes a draft form version after rigorous pre-publish validation.
     */
    @Transactional
    public FormVersion publishVersion(Long formId, Integer versionNumber) {
        FormVersion versionToPublish = versionRepository.findByFormIdAndVersionNumber(formId, versionNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Form version not found with formId: " + formId + ", version: " + versionNumber));

        if (versionToPublish.getStatus() == FormStatus.PUBLISHED) {
            return versionToPublish; // Already published
        }

        if (versionToPublish.getStatus() == FormStatus.ARCHIVED) {
            throw new IllegalFormStateException("Cannot publish an ARCHIVED form version");
        }

        // 1. Pre-publish integrity validations
        validatePrePublishIntegrity(versionToPublish);

        // 2. Archive existing published version for this form if any
        Optional<FormVersion> existingPublishedOpt = versionRepository.findByFormFormCodeAndStatus(
            versionToPublish.getForm().getFormCode(),
            FormStatus.PUBLISHED
        );

        if (existingPublishedOpt.isPresent()) {
            FormVersion currentPublished = existingPublishedOpt.get();
            if (!currentPublished.getId().equals(versionToPublish.getId())) {
                currentPublished.setStatus(FormStatus.ARCHIVED);
                versionRepository.save(currentPublished);
                auditService.logEvent(
                    formId,
                    currentPublished.getId(),
                    versionToPublish.getForm().getFormCode(),
                    AuditAction.VERSION_ARCHIVED,
                    "SYSTEM",
                    "Archived version " + currentPublished.getVersionNumber() + " upon publishing version " + versionNumber
                );
            }
        }

        // 3. Publish target version
        versionToPublish.setStatus(FormStatus.PUBLISHED);
        versionToPublish.setPublishedAt(LocalDateTime.now());
        FormVersion savedVersion = versionRepository.save(versionToPublish);

        auditService.logEvent(
            formId,
            savedVersion.getId(),
            savedVersion.getForm().getFormCode(),
            AuditAction.VERSION_PUBLISHED,
            "SYSTEM",
            "Published version " + versionNumber
        );

        return savedVersion;
    }

    /**
     * Validates form schema completeness prior to publishing.
     */
    public void validatePrePublishIntegrity(FormVersion version) {
        Form form = version.getForm();
        if (form == null || form.getTitle().isBlank() || form.getFormCode().isBlank()) {
            throw new InvalidSchemaException("Form metadata (title, formCode) is invalid or blank");
        }

        List<FormSection> sections = version.getSections();
        if (sections == null || sections.isEmpty()) {
            throw new InvalidSchemaException("Cannot publish form version without at least one section");
        }

        Set<String> fieldCodes = new HashSet<>();
        boolean hasComponents = false;

        for (FormSection section : sections) {
            if (section.getComponents() != null) {
                for (FormComponent comp : section.getComponents()) {
                    hasComponents = true;
                    String code = comp.getFieldCode();

                    // Check unique field codes
                    if (!fieldCodes.add(code)) {
                        throw new InvalidSchemaException("Duplicate fieldCode found in form version schema: " + code);
                    }

                    // Check selection options for dropdown/radio/checkbox
                    if (comp.getComponentType() == ComponentType.SELECT ||
                        comp.getComponentType() == ComponentType.MULTI_SELECT ||
                        comp.getComponentType() == ComponentType.RADIO ||
                        comp.getComponentType() == ComponentType.CHECKBOX) {
                        if (comp.getOptions() == null || comp.getOptions().isEmpty()) {
                            throw new InvalidSchemaException("Component '" + code + "' of type " + comp.getComponentType() + " requires at least one option");
                        }
                    }
                }
            }
        }

        if (!hasComponents) {
            throw new InvalidSchemaException("Form version must contain at least one field component before publishing");
        }

        // Validate conditional rules
        List<FormCondition> conditions = version.getConditions();
        if (conditions != null && !conditions.isEmpty()) {
            for (FormCondition cond : conditions) {
                if (cond.getTriggerComponent() == null || !fieldCodes.contains(cond.getTriggerComponent().getFieldCode())) {
                    throw new InvalidSchemaException("Condition references invalid trigger component fieldCode");
                }
                if (cond.getTargetComponent() == null || !fieldCodes.contains(cond.getTargetComponent().getFieldCode())) {
                    throw new InvalidSchemaException("Condition references invalid target component fieldCode");
                }
            }
            // Check circular dependencies
            conditionalLogicService.detectCircularDependencies(conditions);
        }
    }
}
