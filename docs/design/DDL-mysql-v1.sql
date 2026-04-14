-- PCH (mng) 스키마 v1 — JPA 엔티티(com.pch.mng)와 정합
-- DB: MySQL 8.x, utf8mb4
-- Task: T-200 | 운영 정본은 PRD `02-ERD_v4.0`과 diff 검토 후 Flyway 등으로 이관 권장

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS saved_jql_filter_tb;
DROP TABLE IF EXISTS dashboard_gadget_tb;
DROP TABLE IF EXISTS dashboard_tb;
DROP TABLE IF EXISTS workflow_transition_tb;
DROP TABLE IF EXISTS audit_log_tb;
DROP TABLE IF EXISTS attachment_tb;
DROP TABLE IF EXISTS comment_tb;
DROP TABLE IF EXISTS issue_link_tb;
DROP TABLE IF EXISTS issue_watcher_tb;
DROP TABLE IF EXISTS issue_fix_version_tb;
DROP TABLE IF EXISTS issue_component_tb;
DROP TABLE IF EXISTS issue_label_tb;
DROP TABLE IF EXISTS issue_tb;
DROP TABLE IF EXISTS release_version_tb;
DROP TABLE IF EXISTS sprint_tb;
DROP TABLE IF EXISTS wip_limit_tb;
DROP TABLE IF EXISTS component_tb;
DROP TABLE IF EXISTS project_member_tb;
DROP TABLE IF EXISTS project_tb;
DROP TABLE IF EXISTS label_tb;
DROP TABLE IF EXISTS user_account_tb;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE user_account_tb (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE label_tb (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE project_tb (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `key` VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    board_type VARCHAR(50) NOT NULL,
    lead_id BIGINT NULL,
    archived TINYINT(1) NOT NULL DEFAULT 0,
    issue_sequence BIGINT NOT NULL DEFAULT 0,
    auto_archive_done_after_days INT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    CONSTRAINT fk_project_lead FOREIGN KEY (lead_id) REFERENCES user_account_tb (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE project_member_tb (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    joined_at DATETIME(6) NULL,
    UNIQUE KEY uk_project_member (project_id, user_id),
    CONSTRAINT fk_pm_project FOREIGN KEY (project_id) REFERENCES project_tb (id),
    CONSTRAINT fk_pm_user FOREIGN KEY (user_id) REFERENCES user_account_tb (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE component_tb (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    lead_id BIGINT NULL,
    CONSTRAINT fk_comp_project FOREIGN KEY (project_id) REFERENCES project_tb (id),
    CONSTRAINT fk_comp_lead FOREIGN KEY (lead_id) REFERENCES user_account_tb (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE wip_limit_tb (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    max_issues INT NOT NULL,
    CONSTRAINT fk_wip_project FOREIGN KEY (project_id) REFERENCES project_tb (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sprint_tb (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    goal_points INT NULL,
    created_at DATETIME(6) NULL,
    CONSTRAINT fk_sprint_project FOREIGN KEY (project_id) REFERENCES project_tb (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE release_version_tb (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    release_date DATE NULL,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NULL,
    CONSTRAINT fk_rv_project FOREIGN KEY (project_id) REFERENCES project_tb (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE issue_tb (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    issue_key VARCHAR(255) NOT NULL UNIQUE,
    project_id BIGINT NOT NULL,
    issue_type VARCHAR(50) NOT NULL,
    summary VARCHAR(255) NOT NULL,
    description TEXT NULL,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    story_points INT NULL,
    assignee_id BIGINT NULL,
    reporter_id BIGINT NOT NULL,
    parent_id BIGINT NULL,
    sprint_id BIGINT NULL,
    backlog_rank BIGINT NOT NULL DEFAULT 0,
    security_level VARCHAR(50) NULL,
    epic_start_date DATE NULL,
    epic_end_date DATE NULL,
    archived TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    CONSTRAINT fk_issue_project FOREIGN KEY (project_id) REFERENCES project_tb (id),
    CONSTRAINT fk_issue_assignee FOREIGN KEY (assignee_id) REFERENCES user_account_tb (id),
    CONSTRAINT fk_issue_reporter FOREIGN KEY (reporter_id) REFERENCES user_account_tb (id),
    CONSTRAINT fk_issue_parent FOREIGN KEY (parent_id) REFERENCES issue_tb (id),
    CONSTRAINT fk_issue_sprint FOREIGN KEY (sprint_id) REFERENCES sprint_tb (id),
    KEY idx_issue_project_status (project_id, status),
    KEY idx_issue_assignee (assignee_id),
    KEY idx_issue_sprint (sprint_id),
    KEY idx_issue_project_type_epic_start (project_id, issue_type, epic_start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE issue_label_tb (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    issue_id BIGINT NOT NULL,
    label_id BIGINT NOT NULL,
    UNIQUE KEY uk_issue_label (issue_id, label_id),
    CONSTRAINT fk_il_issue FOREIGN KEY (issue_id) REFERENCES issue_tb (id),
    CONSTRAINT fk_il_label FOREIGN KEY (label_id) REFERENCES label_tb (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE issue_component_tb (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    issue_id BIGINT NOT NULL,
    component_id BIGINT NOT NULL,
    UNIQUE KEY uk_issue_component (issue_id, component_id),
    CONSTRAINT fk_ic_issue FOREIGN KEY (issue_id) REFERENCES issue_tb (id),
    CONSTRAINT fk_ic_component FOREIGN KEY (component_id) REFERENCES component_tb (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE issue_fix_version_tb (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    issue_id BIGINT NOT NULL,
    version_id BIGINT NOT NULL,
    UNIQUE KEY uk_issue_version (issue_id, version_id),
    CONSTRAINT fk_ifv_issue FOREIGN KEY (issue_id) REFERENCES issue_tb (id),
    CONSTRAINT fk_ifv_version FOREIGN KEY (version_id) REFERENCES release_version_tb (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE issue_watcher_tb (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    issue_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    UNIQUE KEY uk_issue_watcher (issue_id, user_id),
    CONSTRAINT fk_iw_issue FOREIGN KEY (issue_id) REFERENCES issue_tb (id),
    CONSTRAINT fk_iw_user FOREIGN KEY (user_id) REFERENCES user_account_tb (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE issue_link_tb (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    source_issue_id BIGINT NOT NULL,
    target_issue_id BIGINT NOT NULL,
    link_type VARCHAR(50) NOT NULL,
    CONSTRAINT fk_link_src FOREIGN KEY (source_issue_id) REFERENCES issue_tb (id),
    CONSTRAINT fk_link_tgt FOREIGN KEY (target_issue_id) REFERENCES issue_tb (id),
    KEY idx_link_src (source_issue_id),
    KEY idx_link_tgt (target_issue_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE comment_tb (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    issue_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    body TEXT NOT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    CONSTRAINT fk_c_issue FOREIGN KEY (issue_id) REFERENCES issue_tb (id),
    CONSTRAINT fk_c_author FOREIGN KEY (author_id) REFERENCES user_account_tb (id),
    KEY idx_comment_issue (issue_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE attachment_tb (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    issue_id BIGINT NOT NULL,
    uploader_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NULL,
    CONSTRAINT fk_att_issue FOREIGN KEY (issue_id) REFERENCES issue_tb (id),
    CONSTRAINT fk_att_user FOREIGN KEY (uploader_id) REFERENCES user_account_tb (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE audit_log_tb (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    issue_id BIGINT NOT NULL,
    changed_by BIGINT NOT NULL,
    field_name VARCHAR(255) NOT NULL,
    old_value TEXT NULL,
    new_value TEXT NULL,
    changed_at DATETIME(6) NULL,
    CONSTRAINT fk_audit_issue FOREIGN KEY (issue_id) REFERENCES issue_tb (id),
    CONSTRAINT fk_audit_user FOREIGN KEY (changed_by) REFERENCES user_account_tb (id),
    KEY idx_audit_issue (issue_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE workflow_transition_tb (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    issue_id BIGINT NOT NULL,
    from_status VARCHAR(50) NOT NULL,
    to_status VARCHAR(50) NOT NULL,
    changed_by BIGINT NOT NULL,
    condition_note TEXT NULL,
    transitioned_at DATETIME(6) NULL,
    CONSTRAINT fk_wt_issue FOREIGN KEY (issue_id) REFERENCES issue_tb (id),
    CONSTRAINT fk_wt_user FOREIGN KEY (changed_by) REFERENCES user_account_tb (id),
    KEY idx_wt_issue (issue_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE dashboard_tb (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    is_shared TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME(6) NULL,
    CONSTRAINT fk_dash_owner FOREIGN KEY (owner_id) REFERENCES user_account_tb (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE dashboard_gadget_tb (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dashboard_id BIGINT NOT NULL,
    gadget_type VARCHAR(255) NOT NULL,
    position INT NOT NULL,
    config_json TEXT NULL,
    CONSTRAINT fk_gadget_dash FOREIGN KEY (dashboard_id) REFERENCES dashboard_tb (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- FR-016 저장 JQL 필터 (T-603)
CREATE TABLE saved_jql_filter_tb (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    jql VARCHAR(4000) NOT NULL,
    created_at DATETIME(6) NULL,
    CONSTRAINT fk_sjf_owner FOREIGN KEY (owner_id) REFERENCES user_account_tb (id),
    CONSTRAINT fk_sjf_project FOREIGN KEY (project_id) REFERENCES project_tb (id),
    KEY idx_sjf_project_owner (project_id, owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- 기존 DB 마이그레이션: Epic 로드맵 기간 (FR-012, T-506)
-- Flyway 미사용 시 수동 실행. 이미 컬럼이 있으면 스킵.
-- ---------------------------------------------------------------------------
-- ALTER TABLE issue_tb ADD COLUMN epic_start_date DATE NULL AFTER security_level;
-- ALTER TABLE issue_tb ADD COLUMN epic_end_date DATE NULL AFTER epic_start_date;
-- CREATE INDEX idx_issue_project_type_epic_start ON issue_tb (project_id, issue_type, epic_start_date);
-- T-617: 이슈 아카이브·프로젝트 자동 아카이브 일수 (기존 DB 마이그레이션용)
-- ALTER TABLE project_tb ADD COLUMN auto_archive_done_after_days INT NULL AFTER issue_sequence;
-- ALTER TABLE issue_tb ADD COLUMN archived TINYINT(1) NOT NULL DEFAULT 0 AFTER epic_end_date;
