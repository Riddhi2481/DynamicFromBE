package com.example.praticalTestBE.service;

import com.example.praticalTestBE.domain.entity.FormAudit;
import com.example.praticalTestBE.domain.enums.AuditAction;
import com.example.praticalTestBE.repository.FormAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for logging immutable audit trails for form lifecycle operations.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final FormAuditRepository auditRepository;

    @Transactional
    public void logEvent(Long formId, Long formVersionId, String formCode, AuditAction action, String performedBy, String details) {
        FormAudit audit = FormAudit.builder()
            .formId(formId)
            .formVersionId(formVersionId)
            .formCode(formCode)
            .action(action)
            .performedBy(performedBy != null ? performedBy : "SYSTEM")
            .details(details)
            .timestamp(LocalDateTime.now())
            .build();
        auditRepository.save(audit);
    }
}
