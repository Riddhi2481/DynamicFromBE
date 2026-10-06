package com.example.praticalTestBE.repository;

import com.example.praticalTestBE.domain.entity.FormAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing FormAudit immutable log entities.
 */
@Repository
public interface FormAuditRepository extends JpaRepository<FormAudit, Long> {

    /**
     * Find audit log records for a specific form ID ordered by timestamp descending.
     */
    List<FormAudit> findByFormIdOrderByTimestampDesc(Long formId);

    /**
     * Find audit log records for a specific form code ordered by timestamp descending.
     */
    List<FormAudit> findByFormCodeOrderByTimestampDesc(String formCode);
}
