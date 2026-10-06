package com.example.praticalTestBE.domain.enums;

/**
 * Represents the lifecycle status of a form version.
 */
public enum FormStatus {
    /**
     * Mutable draft version being created or modified by an administrator.
     */
    DRAFT,

    /**
     * Immutable published version currently active for end-user submissions.
     */
    PUBLISHED,

    /**
     * Immutable historical version that was previously published but superseded by a newer version.
     */
    ARCHIVED
}
