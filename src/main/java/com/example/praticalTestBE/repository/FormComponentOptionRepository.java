package com.example.praticalTestBE.repository;

import com.example.praticalTestBE.domain.entity.FormComponentOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing FormComponentOption entities.
 */
@Repository
public interface FormComponentOptionRepository extends JpaRepository<FormComponentOption, Long> {

    /**
     * Find all options for a component sorted by sortOrder ascending.
     */
    List<FormComponentOption> findByComponentIdOrderBySortOrderAsc(Long componentId);

    /**
     * Delete options associated with a component ID.
     */
    void deleteByComponentId(Long componentId);
}
