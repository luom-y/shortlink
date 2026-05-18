-- ============================================================
-- Shortlink Platform - Database Schema
-- MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS shortlink
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE shortlink;

-- ============================================================
-- User table
-- ============================================================
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       NOT NULL COMMENT 'Snowflake ID',
    `username`    VARCHAR(64)  NOT NULL COMMENT 'Username',
    `password`    VARCHAR(128) NOT NULL COMMENT 'BCrypt hashed password',
    `email`       VARCHAR(128) DEFAULT NULL COMMENT 'Email',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT 'Phone number',
    `role`        VARCHAR(32)  NOT NULL DEFAULT 'USER' COMMENT 'Role: USER, ADMIN',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT 'Status: 1=active, 0=disabled',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0=normal, 1=deleted',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_username` (`username`),
    INDEX `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User';

-- ============================================================
-- Short link table
-- ============================================================
CREATE TABLE IF NOT EXISTS `short_link` (
    `id`           BIGINT        NOT NULL COMMENT 'Snowflake ID',
    `short_code`   VARCHAR(16)   NOT NULL COMMENT 'Short code (Base62)',
    `original_url` TEXT          NOT NULL COMMENT 'Original long URL',
    `title`        VARCHAR(256)  DEFAULT NULL COMMENT 'Page title',
    `user_id`      BIGINT        NOT NULL COMMENT 'Creator user ID',
    `expire_time`  DATETIME      DEFAULT NULL COMMENT 'Expiry time, NULL = never',
    `total_clicks` BIGINT        NOT NULL DEFAULT 0 COMMENT 'Total clicks (denormalized)',
    `status`       TINYINT       NOT NULL DEFAULT 1 COMMENT 'Status: 1=active, 0=disabled',
    `deleted`      TINYINT       NOT NULL DEFAULT 0 COMMENT 'Logical delete',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_short_code` (`short_code`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_expire_time` (`expire_time`),
    INDEX `idx_status_deleted` (`status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Short link';

-- ============================================================
-- Click record table (statistics)
-- ============================================================
CREATE TABLE IF NOT EXISTS `click_record` (
    `id`           BIGINT   NOT NULL AUTO_INCREMENT COMMENT 'Auto increment ID',
    `short_code`   VARCHAR(16) NOT NULL COMMENT 'Short code',
    `ip`           VARCHAR(45) NOT NULL COMMENT 'Visitor IP',
    `user_agent`   VARCHAR(512) DEFAULT NULL COMMENT 'User-Agent',
    `referer`      VARCHAR(1024) DEFAULT NULL COMMENT 'Referer',
    `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Click time',
    PRIMARY KEY (`id`),
    INDEX `idx_short_code_time` (`short_code`, `create_time`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Click record';

-- ============================================================
-- Daily statistics summary table
-- ============================================================
CREATE TABLE IF NOT EXISTS `stats_daily` (
    `id`           BIGINT   NOT NULL AUTO_INCREMENT COMMENT 'Auto increment ID',
    `short_code`   VARCHAR(16) NOT NULL COMMENT 'Short code',
    `stats_date`   DATE     NOT NULL COMMENT 'Statistics date',
    `pv`           BIGINT   NOT NULL DEFAULT 0 COMMENT 'Page views',
    `uv`           BIGINT   NOT NULL DEFAULT 0 COMMENT 'Unique visitors (by IP)',
    `ip_count`     BIGINT   NOT NULL DEFAULT 0 COMMENT 'Unique IP count',
    `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_short_code_date` (`short_code`, `stats_date`),
    INDEX `idx_stats_date` (`stats_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Daily statistics summary';
