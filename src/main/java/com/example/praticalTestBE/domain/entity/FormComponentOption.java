package com.example.praticalTestBE.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Objects;

/**
 * Entity representing dynamic choices for selection components (SELECT, RADIO, CHECKBOX).
 */
@Entity
@Table(
    name = "tbl_form_component_options",
    indexes = {
        @Index(name = "idx_option_component", columnList = "component_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FormComponentOption extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "component_id", nullable = false, foreignKey = @ForeignKey(name = "fk_option_component"))
    @JsonBackReference("component-options")
    private FormComponent component;

    @NotBlank(message = "Option label is mandatory")
    @Size(max = 255)
    @Column(name = "option_label", nullable = false, length = 255)
    private String optionLabel;

    @NotBlank(message = "Option value is mandatory")
    @Size(max = 255)
    @Column(name = "option_value", nullable = false, length = 255)
    private String optionValue;

    @NotNull(message = "Sort order is mandatory")
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormComponentOption that = (FormComponentOption) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
