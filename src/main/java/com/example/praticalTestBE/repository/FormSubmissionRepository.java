package com.example.praticalTestBE.repository;

import com.example.praticalTestBE.domain.entity.FormSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing FormSubmission entities.
 */
@Repository
public interface FormSubmissionRepository extends JpaRepository<FormSubmission, Long> {

    /**
     * Find submissions by form code ordered by submitted timestamp descending (paginated).
     */
    Page<FormSubmission> findByFormVersionFormFormCodeOrderBySubmittedAtDesc(String formCode, Pageable pageable);

    /**
     * Find submissions for a specific form version ID.
     */
    Page<FormSubmission> findByFormVersionIdOrderBySubmittedAtDesc(Long formVersionId, Pageable pageable);

    /**
     * Find a submission by its unique business code.
     */
    Optional<FormSubmission> findBySubmissionCode(String submissionCode);

    /**
     * Optimized fetch query loading submission details with values.
     */
    @Query("""
        SELECT DISTINCT s FROM FormSubmission s
        LEFT JOIN FETCH s.values v
        WHERE s.id = :submissionId
    """)
    Optional<FormSubmission> findByIdWithValues(@Param("submissionId") Long submissionId);

    /**
     * Optimized fetch query loading submission details by submission code with values.
     */
    @Query("""
        SELECT DISTINCT s FROM FormSubmission s
        LEFT JOIN FETCH s.values v
        WHERE s.submissionCode = :submissionCode
    """)
    Optional<FormSubmission> findBySubmissionCodeWithValues(@Param("submissionCode") String submissionCode);

    /**
     * Count total submissions for a form code.
     */
    long countByFormVersionFormFormCode(String formCode);
}
