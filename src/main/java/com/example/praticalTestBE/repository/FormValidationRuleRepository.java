package com.example.praticalTestBE.repository;

import com.example.praticalTestBE.domain.entity.FormValidationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing FormValidationRule entities.
 */
@Repository
public interface FormValidationRuleRepository extends JpaRepository<FormValidationRule, Long> {

    /**
     * Find all validation rules associated with a component ID.
     */
    List<FormValidationRule> findByComponentId(Long componentId);

    /**
     * Delete validation rules associated with a component ID.
     */
    void deleteByComponentId(Long componentId);
}
