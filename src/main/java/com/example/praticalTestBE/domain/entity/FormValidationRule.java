package com.example.praticalTestBE.domain.entity;

import com.example.praticalTestBE.domain.enums.ValidationType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Objects;

/**
 * Entity representing dynamic validation rules associated with a form component.
 */
@Entity
@Table(
    name = "tbl_form_validation_rules",
    indexes = {
        @Index(name = "idx_val_rule_component", columnList = "component_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FormValidationRule extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "component_id", nullable = false, foreignKey = @ForeignKey(name = "fk_val_rule_component"))
    @JsonBackReference("component-validation-rules")
    private FormComponent component;

    @NotNull(message = "Validation type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "validation_type", nullable = false, length = 50)
    private ValidationType validationType;

    @Column(name = "rule_value", columnDefinition = "TEXT")
    private String ruleValue;

    @Size(max = 255)
    @Column(name = "error_message", length = 255)
    private String errorMessage;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormValidationRule that = (FormValidationRule) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
