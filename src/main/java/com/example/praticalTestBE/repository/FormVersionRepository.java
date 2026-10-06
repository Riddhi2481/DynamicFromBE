package com.example.praticalTestBE.repository;

import com.example.praticalTestBE.domain.entity.FormVersion;
import com.example.praticalTestBE.domain.enums.FormStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing FormVersion entities.
 */
@Repository
public interface FormVersionRepository extends JpaRepository<FormVersion, Long> {

    /**
     * Find all versions associated with a form ID, sorted by version number descending.
     */
    List<FormVersion> findByFormIdOrderByVersionNumberDesc(Long formId);

    /**
     * Find a specific form version by form ID and version number.
     */
    Optional<FormVersion> findByFormIdAndVersionNumber(Long formId, Integer versionNumber);

    /**
     * Find a specific form version by form code and version number.
     */
    Optional<FormVersion> findByFormFormCodeAndVersionNumber(String formCode, Integer versionNumber);

    /**
     * Find the active published version for a given form code.
     */
    Optional<FormVersion> findByFormFormCodeAndStatus(String formCode, FormStatus status);

    /**
     * Optimized fetch query to load the active published form version with its sections and components.
     */
    @Query("""
        SELECT DISTINCT fv FROM FormVersion fv
        LEFT JOIN FETCH fv.sections s
        LEFT JOIN FETCH s.components c
        WHERE fv.form.formCode = :formCode AND fv.status = :status
    """)
    Optional<FormVersion> findPublishedVersionWithSchema(
        @Param("formCode") String formCode,
        @Param("status") FormStatus status
    );

    /**
     * Find current DRAFT version for a form if it exists.
     */
    Optional<FormVersion> findByFormIdAndStatus(Long formId, FormStatus status);

    /**
     * Query the maximum version number for a given form.
     */
    @Query("SELECT COALESCE(MAX(fv.versionNumber), 0) FROM FormVersion fv WHERE fv.form.id = :formId")
    Integer findMaxVersionNumberByFormId(@Param("formId") Long formId);

    /**
     * Check if a specific form version exists.
     */
    boolean existsByFormIdAndVersionNumber(Long formId, Integer versionNumber);
}
