package com.example.praticalTestBE.repository;

import com.example.praticalTestBE.domain.entity.FormCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing FormCondition entities.
 */
@Repository
public interface FormConditionRepository extends JpaRepository<FormCondition, Long> {

    /**
     * Find all conditional rules for a specific form version.
     */
    List<FormCondition> findByFormVersionId(Long formVersionId);

    /**
     * Find conditions triggered by a specific component ID.
     */
    List<FormCondition> findByTriggerComponentId(Long triggerComponentId);

    /**
     * Find conditions targeting a specific component ID.
     */
    List<FormCondition> findByTargetComponentId(Long targetComponentId);

    /**
     * Fetch conditions for a form version with trigger and target components loaded.
     */
    @Query("""
        SELECT fc FROM FormCondition fc
        JOIN FETCH fc.triggerComponent tc
        JOIN FETCH fc.targetComponent tac
        WHERE fc.formVersion.id = :formVersionId
    """)
    List<FormCondition> findByFormVersionIdWithComponents(@Param("formVersionId") Long formVersionId);
}
