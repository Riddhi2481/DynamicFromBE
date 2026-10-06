package com.example.praticalTestBE.domain.entity;

import com.example.praticalTestBE.domain.enums.AuditAction;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing an immutable audit log entry tracking form lifecycle events.
 */
@Entity
@Table(
    name = "tbl_form_audits",
    indexes = {
        @Index(name = "idx_audit_form_id", columnList = "form_id"),
        @Index(name = "idx_audit_form_code", columnList = "form_code"),
        @Index(name = "idx_audit_action", columnList = "action")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FormAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "form_id")
    private Long formId;

    @Column(name = "form_version_id")
    private Long formVersionId;

    @Size(max = 100)
    @Column(name = "form_code", length = 100)
    private String formCode;

    @NotNull(message = "Audit action is mandatory")
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AuditAction action;

    @Size(max = 100)
    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @CreatedDate
    @NotNull
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormAudit formAudit = (FormAudit) o;
        return id != null && Objects.equals(id, formAudit.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
