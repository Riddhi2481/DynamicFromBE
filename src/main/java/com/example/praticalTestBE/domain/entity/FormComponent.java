package com.example.praticalTestBE.domain.entity;

import com.example.praticalTestBE.domain.enums.ComponentType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing an individual dynamic field component within a form section.
 */
@Entity
@Table(
    name = "tbl_form_components",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_version_field_code", columnNames = {"form_version_id", "field_code"})
    },
    indexes = {
        @Index(name = "idx_component_version", columnList = "form_version_id"),
        @Index(name = "idx_component_field_code", columnList = "field_code"),
        @Index(name = "idx_component_section", columnList = "section_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FormComponent extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "form_version_id", nullable = false, foreignKey = @ForeignKey(name = "fk_component_version"))
    @JsonBackReference("version-components")
    private FormVersion formVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false, foreignKey = @ForeignKey(name = "fk_component_section"))
    @JsonBackReference("section-components")
    private FormSection section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_component_id", foreignKey = @ForeignKey(name = "fk_component_parent"))
    @JsonBackReference("parent-child-components")
    private FormComponent parentComponent;

    @NotBlank(message = "Field code is mandatory")
    @Size(max = 100)
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Field code must be alphanumeric with underscores")
    @Column(name = "field_code", nullable = false, length = 100)
    private String fieldCode;

    @NotBlank(message = "Label is mandatory")
    @Size(max = 255)
    @Column(name = "label", nullable = false, length = 255)
    private String label;

    @NotNull(message = "Component type is mandatory")
    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false, length = 50)
    private ComponentType componentType;

    @Column(name = "placeholder", length = 255)
    private String placeholder;

    @Column(name = "help_text", length = 500)
    private String helpText;

    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;

    @NotNull(message = "Sort order is mandatory")
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Builder.Default
    @Column(name = "is_required", nullable = false)
    private boolean required = false;

    @Builder.Default
    @Column(name = "is_read_only", nullable = false)
    private boolean readOnly = false;

    @Builder.Default
    @Column(name = "is_disabled", nullable = false)
    private boolean disabled = false;

    @Builder.Default
    @Column(name = "is_visible", nullable = false)
    private boolean visible = true;

    @Builder.Default
    @OneToMany(mappedBy = "parentComponent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    @JsonManagedReference("parent-child-components")
    private List<FormComponent> childComponents = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    @JsonManagedReference("component-options")
    private List<FormComponentOption> options = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("component-validation-rules")
    private List<FormValidationRule> validationRules = new ArrayList<>();

    public void addOption(FormComponentOption option) {
        options.add(option);
        option.setComponent(this);
    }

    public void addValidationRule(FormValidationRule rule) {
        validationRules.add(rule);
        rule.setComponent(this);
    }

    public void addChildComponent(FormComponent child) {
        childComponents.add(child);
        child.setParentComponent(this);
        child.setFormVersion(this.formVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormComponent that = (FormComponent) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
