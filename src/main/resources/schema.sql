-- =============================================================================
-- MySQL Schema Script: Configuration-Driven Dynamic Form Builder
-- Database: practical_test
-- Engine: InnoDB | Character Set: utf8mb4 | Collation: utf8mb4_unicode_ci
-- =============================================================================

CREATE DATABASE IF NOT EXISTS `practical_test` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `practical_test`;

-- -----------------------------------------------------------------------------
-- 1. Table: tbl_forms (Aggregate Root)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tbl_forms` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `form_code` VARCHAR(100) NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `description` TEXT NULL,
    `category` VARCHAR(100) NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `uk_form_code` UNIQUE (`form_code`),
    INDEX `idx_form_code` (`form_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. Table: tbl_form_versions
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tbl_form_versions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `form_id` BIGINT NOT NULL,
    `version_number` INT NOT NULL,
    `status` VARCHAR(20) NOT NULL,
    `published_at` DATETIME(6) NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `uk_form_version_number` UNIQUE (`form_id`, `version_number`),
    CONSTRAINT `fk_version_form` FOREIGN KEY (`form_id`) REFERENCES `tbl_forms` (`id`) ON DELETE CASCADE,
    INDEX `idx_version_form_status` (`form_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. Table: tbl_form_sections
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tbl_form_sections` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `form_version_id` BIGINT NOT NULL,
    `section_code` VARCHAR(100) NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `description` TEXT NULL,
    `sort_order` INT NOT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_section_version` FOREIGN KEY (`form_version_id`) REFERENCES `tbl_form_versions` (`id`) ON DELETE CASCADE,
    INDEX `idx_section_version` (`form_version_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. Table: tbl_form_components
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tbl_form_components` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `form_version_id` BIGINT NOT NULL,
    `section_id` BIGINT NOT NULL,
    `parent_component_id` BIGINT NULL,
    `field_code` VARCHAR(100) NOT NULL,
    `label` VARCHAR(255) NOT NULL,
    `component_type` VARCHAR(50) NOT NULL,
    `placeholder` VARCHAR(255) NULL,
    `default_value` TEXT NULL,
    `sort_order` INT NOT NULL,
    `is_required` BOOLEAN NOT NULL DEFAULT FALSE,
    `is_visible` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `uk_version_field_code` UNIQUE (`form_version_id`, `field_code`),
    CONSTRAINT `fk_component_version` FOREIGN KEY (`form_version_id`) REFERENCES `tbl_form_versions` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_component_section` FOREIGN KEY (`section_id`) REFERENCES `tbl_form_sections` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_component_parent` FOREIGN KEY (`parent_component_id`) REFERENCES `tbl_form_components` (`id`) ON DELETE SET NULL,
    INDEX `idx_component_version` (`form_version_id`),
    INDEX `idx_component_field_code` (`field_code`),
    INDEX `idx_component_section` (`section_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5. Table: tbl_form_component_options
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tbl_form_component_options` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `component_id` BIGINT NOT NULL,
    `option_label` VARCHAR(255) NOT NULL,
    `option_value` VARCHAR(255) NOT NULL,
    `sort_order` INT NOT NULL,
    `is_default` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_option_component` FOREIGN KEY (`component_id`) REFERENCES `tbl_form_components` (`id`) ON DELETE CASCADE,
    INDEX `idx_option_component` (`component_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 6. Table: tbl_form_validation_rules
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tbl_form_validation_rules` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `component_id` BIGINT NOT NULL,
    `validation_type` VARCHAR(50) NOT NULL,
    `rule_value` TEXT NULL,
    `error_message` VARCHAR(255) NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_val_rule_component` FOREIGN KEY (`component_id`) REFERENCES `tbl_form_components` (`id`) ON DELETE CASCADE,
    INDEX `idx_val_rule_component` (`component_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 7. Table: tbl_form_conditions
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tbl_form_conditions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `form_version_id` BIGINT NOT NULL,
    `trigger_component_id` BIGINT NOT NULL,
    `operator` VARCHAR(50) NOT NULL,
    `logic_operator` VARCHAR(10) DEFAULT 'AND',
    `trigger_value` TEXT NULL,
    `target_component_id` BIGINT NOT NULL,
    `action` VARCHAR(50) NOT NULL,
    `action_value` TEXT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_condition_version` FOREIGN KEY (`form_version_id`) REFERENCES `tbl_form_versions` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_condition_trigger` FOREIGN KEY (`trigger_component_id`) REFERENCES `tbl_form_components` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_condition_target` FOREIGN KEY (`target_component_id`) REFERENCES `tbl_form_components` (`id`) ON DELETE CASCADE,
    INDEX `idx_condition_version` (`form_version_id`),
    INDEX `idx_condition_trigger` (`trigger_component_id`),
    INDEX `idx_condition_target` (`target_component_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 8. Table: tbl_form_submissions
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tbl_form_submissions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `form_version_id` BIGINT NOT NULL,
    `submission_code` VARCHAR(100) NOT NULL,
    `submitted_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `uk_submission_code` UNIQUE (`submission_code`),
    CONSTRAINT `fk_submission_form_version` FOREIGN KEY (`form_version_id`) REFERENCES `tbl_form_versions` (`id`),
    INDEX `idx_submission_version` (`form_version_id`),
    INDEX `idx_submission_code` (`submission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 9. Table: tbl_form_submission_values
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tbl_form_submission_values` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `submission_id` BIGINT NOT NULL,
    `component_id` BIGINT NULL,
    `field_code` VARCHAR(100) NOT NULL,
    `field_label` VARCHAR(255) NULL,
    `value` TEXT NULL,
    `value_json` TEXT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_sub_val_submission` FOREIGN KEY (`submission_id`) REFERENCES `tbl_form_submissions` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_sub_val_component` FOREIGN KEY (`component_id`) REFERENCES `tbl_form_components` (`id`) ON DELETE SET NULL,
    INDEX `idx_sub_val_submission` (`submission_id`),
    INDEX `idx_sub_val_component` (`component_id`),
    INDEX `idx_sub_val_field_code` (`field_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 10. Table: tbl_form_audits
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tbl_form_audits` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `form_id` BIGINT NULL,
    `form_version_id` BIGINT NULL,
    `form_code` VARCHAR(100) NULL,
    `action` VARCHAR(50) NOT NULL,
    `performed_by` VARCHAR(100) NULL,
    `details` TEXT NULL,
    `timestamp` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_audit_form_id` (`form_id`),
    INDEX `idx_audit_form_code` (`form_code`),
    INDEX `idx_audit_action` (`action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
