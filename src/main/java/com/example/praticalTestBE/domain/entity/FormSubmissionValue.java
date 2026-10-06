package com.example.praticalTestBE.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Objects;

/**
 * Entity representing an individual field value entry within a FormSubmission.
 */
@Entity
@Table(
    name = "tbl_form_submission_values",
    indexes = {
        @Index(name = "idx_sub_val_submission", columnList = "submission_id"),
        @Index(name = "idx_sub_val_component", columnList = "component_id"),
        @Index(name = "idx_sub_val_field_code", columnList = "field_code")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FormSubmissionValue extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sub_val_submission"))
    @JsonBackReference("submission-values")
    private FormSubmission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", foreignKey = @ForeignKey(name = "fk_sub_val_component"))
    private FormComponent component;

    @NotBlank(message = "Field code snapshot is mandatory")
    @Size(max = 100)
    @Column(name = "field_code", nullable = false, length = 100, updatable = false)
    private String fieldCode;

    @Size(max = 255)
    @Column(name = "field_label", length = 255, updatable = false)
    private String fieldLabel;

    @Column(name = "value", columnDefinition = "TEXT")
    private String value;

    @Column(name = "value_json", columnDefinition = "TEXT")
    private String valueJson;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormSubmissionValue that = (FormSubmissionValue) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
