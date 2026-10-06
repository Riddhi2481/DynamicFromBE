package com.example.praticalTestBE.domain.enums;

/**
 * Defines dynamic validation rules applicable to components.
 */
public enum ValidationType {
    REQUIRED,
    MIN,
    MIN_VALUE,
    MAX,
    MAX_VALUE,
    MIN_LENGTH,
    MAX_LENGTH,
    REGEX,
    EMAIL,
    NUMBER,
    DATE,
    FILE_SIZE,
    FILE_TYPE,
    CUSTOM
}
