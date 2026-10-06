package com.example.praticalTestBE.service;

import com.example.praticalTestBE.domain.entity.*;
import com.example.praticalTestBE.domain.enums.AuditAction;
import com.example.praticalTestBE.domain.enums.FormStatus;
import com.example.praticalTestBE.dto.request.CreateVersionRequest;
import com.example.praticalTestBE.dto.request.FormSectionRequest;
import com.example.praticalTestBE.dto.request.ConditionRequest;
import com.example.praticalTestBE.dto.response.*;
import com.example.praticalTestBE.exception.IllegalFormStateException;
import com.example.praticalTestBE.exception.ResourceNotFoundException;
import com.example.praticalTestBE.repository.FormRepository;
import com.example.praticalTestBE.repository.FormVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service managing form versioning, schema cloning, draft creation, and DTO transformations.
 */
@Service
@RequiredArgsConstructor
public class FormVersionService {

    private final FormRepository formRepository;
    private final FormVersionRepository versionRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<VersionHistoryResponse> getVersionHistory(Long formId) {
        if (!formRepository.existsById(formId)) {
            throw new ResourceNotFoundException("Form not found with id: " + formId);
        }
        return versionRepository.findByFormIdOrderByVersionNumberDesc(formId).stream()
            .map(v -> new VersionHistoryResponse(v.getId(), v.getVersionNumber(), v.getStatus(), v.getPublishedAt(), v.getCreatedAt()))
            .toList();
    }

    @Transactional(readOnly = true)
    public FormVersionResponse getVersionDetails(Long formId, Integer versionNumber) {
        FormVersion version = versionRepository.findByFormIdAndVersionNumber(formId, versionNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Form version not found for formId: " + formId + ", version: " + versionNumber));
        return mapToVersionResponse(version);
    }

    @Transactional(readOnly = true)
    public PublishedFormResponse getPublishedSchema(String formCode) {
        FormVersion publishedVersion = versionRepository.findPublishedVersionWithSchema(formCode, FormStatus.PUBLISHED)
            .orElseThrow(() -> new ResourceNotFoundException("No active PUBLISHED version found for formCode: " + formCode));
        return mapToPublishedFormResponse(publishedVersion);
    }

    /**
     * Obtains or creates a mutable DRAFT version for editing.
     * If the current version is PUBLISHED, forks a new DRAFT version automatically.
     */
    @Transactional
    public FormVersion getOrCreateDraftForEdit(Form form) {
        // Check for existing DRAFT version
        var draftOpt = versionRepository.findByFormIdAndStatus(form.getId(), FormStatus.DRAFT);
        if (draftOpt.isPresent()) {
            return draftOpt.get();
        }

        // Clone from latest existing version
        Integer nextVersionNum = versionRepository.findMaxVersionNumberByFormId(form.getId()) + 1;
        FormVersion latestVersion = versionRepository.findByFormIdOrderByVersionNumberDesc(form.getId())
            .stream().findFirst().orElse(null);

        FormVersion newDraft = FormVersion.builder()
            .form(form)
            .versionNumber(nextVersionNum)
            .status(FormStatus.DRAFT)
            .build();

        if (latestVersion != null) {
            cloneSchema(latestVersion, newDraft);
        }

        FormVersion savedDraft = versionRepository.save(newDraft);
        auditService.logEvent(
            form.getId(),
            savedDraft.getId(),
            form.getFormCode(),
            AuditAction.VERSION_CREATED,
            "SYSTEM",
            "Created new draft version " + nextVersionNum
        );
        return savedDraft;
    }

    @Transactional
    public FormVersionResponse createNewDraftVersion(Long formId, CreateVersionRequest request) {
        Form form = formRepository.findById(formId)
            .orElseThrow(() -> new ResourceNotFoundException("Form not found with id: " + formId));

        FormVersion draft = getOrCreateDraftForEdit(form);
        if (request != null && request.sections() != null) {
            buildSchemaFromRequests(draft, request.sections(), request.conditions());
        }
        FormVersion saved = versionRepository.save(draft);
        return mapToVersionResponse(saved);
    }

    /**
     * Builds and updates sections, components, options, and conditions on a draft version.
     */
    public void buildSchemaFromRequests(FormVersion draftVersion, List<FormSectionRequest> sectionReqs, List<ConditionRequest> conditionReqs) {
        if (draftVersion.getStatus() != FormStatus.DRAFT) {
            throw new IllegalFormStateException("Cannot modify schema of a non-DRAFT form version");
        }

        if (draftVersion.getSections() != null && !draftVersion.getSections().isEmpty()) {
            draftVersion.getSections().clear();
            draftVersion.getConditions().clear();
            versionRepository.saveAndFlush(draftVersion);
        }

        Map<String, FormComponent> fieldCodeMap = new HashMap<>();

        if (sectionReqs != null) {
            for (FormSectionRequest secReq : sectionReqs) {
                FormSection section = FormSection.builder()
                    .formVersion(draftVersion)
                    .sectionCode(secReq.sectionCode())
                    .title(secReq.title())
                    .description(secReq.description())
                    .sortOrder(secReq.sortOrder())
                    .build();

                if (secReq.components() != null) {
                    for (var compReq : secReq.components()) {
                        FormComponent comp = FormComponent.builder()
                            .formVersion(draftVersion)
                            .section(section)
                            .fieldCode(compReq.fieldCode())
                            .label(compReq.label())
                            .componentType(compReq.componentType())
                            .placeholder(compReq.placeholder())
                            .defaultValue(compReq.defaultValue())
                            .sortOrder(compReq.sortOrder())
                            .required(compReq.required() != null && compReq.required())
                            .visible(compReq.visible() == null || compReq.visible())
                            .build();

                        if (compReq.options() != null) {
                            for (var optReq : compReq.options()) {
                                FormComponentOption opt = FormComponentOption.builder()
                                    .component(comp)
                                    .optionLabel(optReq.optionLabel())
                                    .optionValue(optReq.optionValue())
                                    .sortOrder(optReq.sortOrder())
                                    .isDefault(optReq.isDefault() != null && optReq.isDefault())
                                    .build();
                                comp.addOption(opt);
                            }
                        }

                        if (compReq.validationRules() != null) {
                            for (var ruleReq : compReq.validationRules()) {
                                FormValidationRule rule = FormValidationRule.builder()
                                    .component(comp)
                                    .validationType(ruleReq.validationType())
                                    .ruleValue(ruleReq.ruleValue())
                                    .errorMessage(ruleReq.errorMessage())
                                    .build();
                                comp.addValidationRule(rule);
                            }
                        }

                        section.addComponent(comp);
                        fieldCodeMap.put(comp.getFieldCode(), comp);
                    }
                }
                draftVersion.addSection(section);
            }
        }

        if (conditionReqs != null) {
            for (ConditionRequest condReq : conditionReqs) {
                FormComponent triggerComp = fieldCodeMap.get(condReq.triggerFieldCode());
                FormComponent targetComp = fieldCodeMap.get(condReq.targetFieldCode());

                if (triggerComp != null && targetComp != null) {
                    FormCondition cond = FormCondition.builder()
                        .formVersion(draftVersion)
                        .triggerComponent(triggerComp)
                        .operator(condReq.operator())
                        .logicOperator(condReq.logicOperator())
                        .triggerValue(condReq.triggerValue())
                        .targetComponent(targetComp)
                        .action(condReq.action())
                        .actionValue(condReq.actionValue())
                        .build();
                    draftVersion.addCondition(cond);
                }
            }
        }
    }

    private void cloneSchema(FormVersion source, FormVersion target) {
        Map<String, FormComponent> compMap = new HashMap<>();
        for (FormSection srcSec : source.getSections()) {
            FormSection newSec = FormSection.builder()
                .formVersion(target)
                .sectionCode(srcSec.getSectionCode())
                .title(srcSec.getTitle())
                .description(srcSec.getDescription())
                .sortOrder(srcSec.getSortOrder())
                .build();

            for (FormComponent srcComp : srcSec.getComponents()) {
                FormComponent newComp = FormComponent.builder()
                    .formVersion(target)
                    .section(newSec)
                    .fieldCode(srcComp.getFieldCode())
                    .label(srcComp.getLabel())
                    .componentType(srcComp.getComponentType())
                    .placeholder(srcComp.getPlaceholder())
                    .defaultValue(srcComp.getDefaultValue())
                    .sortOrder(srcComp.getSortOrder())
                    .required(srcComp.isRequired())
                    .visible(srcComp.isVisible())
                    .build();

                for (FormComponentOption srcOpt : srcComp.getOptions()) {
                    FormComponentOption newOpt = FormComponentOption.builder()
                        .component(newComp)
                        .optionLabel(srcOpt.getOptionLabel())
                        .optionValue(srcOpt.getOptionValue())
                        .sortOrder(srcOpt.getSortOrder())
                        .isDefault(srcOpt.isDefault())
                        .build();
                    newComp.addOption(newOpt);
                }

                for (FormValidationRule srcRule : srcComp.getValidationRules()) {
                    FormValidationRule newRule = FormValidationRule.builder()
                        .component(newComp)
                        .validationType(srcRule.getValidationType())
                        .ruleValue(srcRule.getRuleValue())
                        .errorMessage(srcRule.getErrorMessage())
                        .build();
                    newComp.addValidationRule(newRule);
                }

                newSec.addComponent(newComp);
                compMap.put(newComp.getFieldCode(), newComp);
            }
            target.addSection(newSec);
        }

        for (FormCondition srcCond : source.getConditions()) {
            FormComponent trig = compMap.get(srcCond.getTriggerComponent().getFieldCode());
            FormComponent targ = compMap.get(srcCond.getTargetComponent().getFieldCode());
            if (trig != null && targ != null) {
                FormCondition newCond = FormCondition.builder()
                    .formVersion(target)
                    .triggerComponent(trig)
                    .operator(srcCond.getOperator())
                    .logicOperator(srcCond.getLogicOperator())
                    .triggerValue(srcCond.getTriggerValue())
                    .targetComponent(targ)
                    .action(srcCond.getAction())
                    .actionValue(srcCond.getActionValue())
                    .build();
                target.addCondition(newCond);
            }
        }
    }

    public FormVersionResponse mapToVersionResponse(FormVersion version) {
        List<FormSectionResponse> sectionResponses = new ArrayList<>();
        if (version.getSections() != null) {
            for (FormSection section : version.getSections()) {
                List<ComponentResponse> compResponses = new ArrayList<>();
                if (section.getComponents() != null) {
                    for (FormComponent comp : section.getComponents()) {
                        compResponses.add(mapToComponentResponse(comp));
                    }
                }
                sectionResponses.add(new FormSectionResponse(
                    section.getId(), section.getSectionCode(), section.getTitle(),
                    section.getDescription(), section.getSortOrder(), compResponses
                ));
            }
        }

        List<ConditionResponse> condResponses = new ArrayList<>();
        if (version.getConditions() != null) {
            for (FormCondition cond : version.getConditions()) {
                condResponses.add(new ConditionResponse(
                    cond.getId(),
                    cond.getTriggerComponent() != null ? cond.getTriggerComponent().getFieldCode() : null,
                    cond.getOperator(),
                    cond.getLogicOperator(),
                    cond.getTriggerValue(),
                    cond.getTargetComponent() != null ? cond.getTargetComponent().getFieldCode() : null,
                    cond.getAction(),
                    cond.getActionValue()
                ));
            }
        }

        return new FormVersionResponse(
            version.getId(),
            version.getForm() != null ? version.getForm().getId() : null,
            version.getForm() != null ? version.getForm().getFormCode() : null,
            version.getVersionNumber(),
            version.getStatus(),
            version.getPublishedAt(),
            version.getCreatedAt(),
            version.getUpdatedAt(),
            sectionResponses,
            condResponses
        );
    }

    public PublishedFormResponse mapToPublishedFormResponse(FormVersion version) {
        FormVersionResponse res = mapToVersionResponse(version);
        return new PublishedFormResponse(
            version.getForm().getFormCode(),
            version.getForm().getTitle(),
            version.getForm().getDescription(),
            version.getVersionNumber(),
            res.sections(),
            res.conditions()
        );
    }

    private ComponentResponse mapToComponentResponse(FormComponent comp) {
        List<ComponentOptionResponse> options = comp.getOptions() != null ?
            comp.getOptions().stream().map(o -> new ComponentOptionResponse(o.getId(), o.getOptionLabel(), o.getOptionValue(), o.getSortOrder(), o.isDefault())).toList() : List.of();

        List<ValidationRuleResponse> rules = comp.getValidationRules() != null ?
            comp.getValidationRules().stream().map(r -> new ValidationRuleResponse(r.getId(), r.getValidationType(), r.getRuleValue(), r.getErrorMessage())).toList() : List.of();

        List<ComponentResponse> children = comp.getChildComponents() != null ?
            comp.getChildComponents().stream().map(this::mapToComponentResponse).toList() : List.of();

        return new ComponentResponse(
            comp.getId(), comp.getFieldCode(), comp.getLabel(), comp.getComponentType(),
            comp.getPlaceholder(), comp.getDefaultValue(), comp.getSortOrder(),
            comp.isRequired(), comp.isVisible(), options, rules, children
        );
    }
}
