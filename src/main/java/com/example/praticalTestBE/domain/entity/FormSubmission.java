package com.example.praticalTestBE.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing an individual user submission response to a published FormVersion.
 */
@Entity
@Table(
    name = "tbl_form_submissions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_submission_code", columnNames = {"submission_code"})
    },
    indexes = {
        @Index(name = "idx_submission_version", columnList = "form_version_id"),
        @Index(name = "idx_submission_code", columnList = "submission_code")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FormSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "form_version_id", nullable = false, foreignKey = @ForeignKey(name = "fk_submission_form_version"))
    private FormVersion formVersion;

    @NotBlank(message = "Submission code is mandatory")
    @Size(max = 100)
    @Column(name = "submission_code", nullable = false, length = 100, updatable = false)
    private String submissionCode;

    @CreatedDate
    @NotNull
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Builder.Default
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("submission-values")
    private List<FormSubmissionValue> values = new ArrayList<>();

    public void addValue(FormSubmissionValue value) {
        values.add(value);
        value.setSubmission(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormSubmission that = (FormSubmission) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
