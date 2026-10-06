package com.example.praticalTestBE.repository;

import com.example.praticalTestBE.domain.entity.FormSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing FormSection layout entities.
 */
@Repository
public interface FormSectionRepository extends JpaRepository<FormSection, Long> {

    /**
     * Find all layout sections for a form version ordered by sortOrder ascending.
     */
    List<FormSection> findByFormVersionIdOrderBySortOrderAsc(Long formVersionId);

    /**
     * Find a section by form version ID and section code.
     */
    Optional<FormSection> findByFormVersionIdAndSectionCode(Long formVersionId, String sectionCode);

    /**
     * Delete all sections associated with a form version.
     */
    void deleteByFormVersionId(Long formVersionId);
}
