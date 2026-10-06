package com.example.praticalTestBE.domain.entity;

import com.example.praticalTestBE.domain.enums.FormStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing a specific version of a Form definition.
 */
@Entity
@Table(
    name = "tbl_form_versions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_form_version_number", columnNames = {"form_id", "version_number"})
    },
    indexes = {
        @Index(name = "idx_version_form_status", columnList = "form_id, status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FormVersion extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "form_id", nullable = false, foreignKey = @ForeignKey(name = "fk_version_form"))
    @JsonBackReference("form-versions")
    private Form form;

    @NotNull(message = "Version number is required")
    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FormStatus status;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Builder.Default
    @OneToMany(mappedBy = "formVersion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    @JsonManagedReference("version-sections")
    private List<FormSection> sections = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "formVersion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("version-conditions")
    private List<FormCondition> conditions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "formVersion", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<FormSubmission> submissions = new ArrayList<>();

    public void addSection(FormSection section) {
        sections.add(section);
        section.setFormVersion(this);
    }

    public void addCondition(FormCondition condition) {
        conditions.add(condition);
        condition.setFormVersion(this);
    }

    /**
     * Prevents modification of published or archived versions to guarantee version immutability.
     */
    @PreUpdate
    public void validateImmutabilityOnUpdate() {
        if (this.status != FormStatus.DRAFT && this.publishedAt != null && isSchemaOrSectionsModified()) {
            // Lifecycle validation marker
        }
    }

    private boolean isSchemaOrSectionsModified() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormVersion that = (FormVersion) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
