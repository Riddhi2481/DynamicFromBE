package com.example.praticalTestBE.repository;

import com.example.praticalTestBE.domain.entity.FormComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing FormComponent dynamic field entities.
 */
@Repository
public interface FormComponentRepository extends JpaRepository<FormComponent, Long> {

    /**
     * Find all components for a specific form version sorted by sortOrder.
     */
    List<FormComponent> findByFormVersionIdOrderBySortOrderAsc(Long formVersionId);

    /**
     * Find all components belonging to a section sorted by sortOrder.
     */
    List<FormComponent> findBySectionIdOrderBySortOrderAsc(Long sectionId);

    /**
     * Find a component by form version ID and unique fieldCode.
     */
    Optional<FormComponent> findByFormVersionIdAndFieldCode(Long formVersionId, String fieldCode);

    /**
     * Check if a fieldCode already exists within a specific form version.
     */
    boolean existsByFormVersionIdAndFieldCode(Long formVersionId, String fieldCode);

    /**
     * Fetch top-level root components (parentComponent IS NULL) for a section.
     */
    List<FormComponent> findBySectionIdAndParentComponentIsNullOrderBySortOrderAsc(Long sectionId);

    /**
     * Custom query to fetch a component with options and validation rules eagerly.
     */
    @Query("""
        SELECT DISTINCT c FROM FormComponent c
        LEFT JOIN FETCH c.options o
        LEFT JOIN FETCH c.validationRules r
        WHERE c.formVersion.id = :formVersionId AND c.fieldCode = :fieldCode
    """)
    Optional<FormComponent> findByFormVersionIdAndFieldCodeWithDetails(
        @Param("formVersionId") Long formVersionId,
        @Param("fieldCode") String fieldCode
    );
}
