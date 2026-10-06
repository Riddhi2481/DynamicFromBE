package com.example.praticalTestBE.repository;

import com.example.praticalTestBE.domain.entity.Form;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Form aggregate root entities.
 */
@Repository
public interface FormRepository extends JpaRepository<Form, Long> {

    /**
     * Find a form by its unique business code.
     */
    Optional<Form> findByFormCode(String formCode);

    /**
     * Check if a form exists with the given form code.
     */
    boolean existsByFormCode(String formCode);

    /**
     * Retrieve all forms ordered by last modification date descending.
     */
    List<Form> findAllByOrderByUpdatedAtDesc();

    /**
     * Paginated retrieval of forms ordered by creation date descending.
     */
    Page<Form> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
