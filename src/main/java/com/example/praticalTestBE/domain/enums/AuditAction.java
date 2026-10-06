package com.example.praticalTestBE.domain.enums;

/**
 * Defines audited event actions for form lifecycle tracking.
 */
public enum AuditAction {
    FORM_CREATED,
    FORM_UPDATED,
    FORM_DELETED,
    VERSION_CREATED,
    VERSION_UPDATED,
    VERSION_PUBLISHED,
    VERSION_ARCHIVED,
    SUBMISSION_CREATED
}
