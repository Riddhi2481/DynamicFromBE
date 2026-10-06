package com.example.praticalTestBE.repository;

import com.example.praticalTestBE.domain.entity.FormSubmissionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing FormSubmissionValue entities.
 */
@Repository
public interface FormSubmissionValueRepository extends JpaRepository<FormSubmissionValue, Long> {

    /**
     * Find all value entries for a specific submission ID.
     */
    List<FormSubmissionValue> findBySubmissionId(Long submissionId);

    /**
     * Find a specific field value entry for a submission by fieldCode.
     */
    Optional<FormSubmissionValue> findBySubmissionIdAndFieldCode(Long submissionId, String fieldCode);
}
