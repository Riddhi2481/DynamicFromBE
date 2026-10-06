package com.example.praticalTestBE.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing a section layout container within a FormVersion.
 */
@Entity
@Table(
    name = "tbl_form_sections",
    indexes = {
        @Index(name = "idx_section_version", columnList = "form_version_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FormSection extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "form_version_id", nullable = false, foreignKey = @ForeignKey(name = "fk_section_version"))
    @JsonBackReference("version-sections")
    private FormVersion formVersion;

    @NotBlank(message = "Section code is required")
    @Size(max = 100)
    @Column(name = "section_code", nullable = false, length = 100)
    private String sectionCode;

    @NotBlank(message = "Section title is required")
    @Size(max = 255)
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Sort order is required")
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Builder.Default
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    @JsonManagedReference("section-components")
    private List<FormComponent> components = new ArrayList<>();

    public void addComponent(FormComponent component) {
        components.add(component);
        component.setSection(this);
        if (this.formVersion != null) {
            component.setFormVersion(this.formVersion);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormSection that = (FormSection) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
