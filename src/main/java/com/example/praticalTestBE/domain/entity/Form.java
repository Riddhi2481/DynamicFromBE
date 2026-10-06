package com.example.praticalTestBE.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root entity representing a Form template.
 */
@Entity
@Table(
    name = "tbl_forms",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_form_code", columnNames = {"form_code"})
    },
    indexes = {
        @Index(name = "idx_form_code", columnList = "form_code")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Form extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Form code is mandatory")
    @Size(max = 100, message = "Form code cannot exceed 100 characters")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Form code must contain only alphanumeric characters and underscores")
    @Column(name = "form_code", nullable = false, length = 100, updatable = false)
    private String formCode;

    @NotBlank(message = "Title is mandatory")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("versionNumber DESC")
    @JsonManagedReference("form-versions")
    private List<FormVersion> versions = new ArrayList<>();

    public void addVersion(FormVersion version) {
        versions.add(version);
        version.setForm(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Form form = (Form) o;
        return id != null && Objects.equals(id, form.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
