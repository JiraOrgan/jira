-- H2(dev) 기본 시드: 로컬 로그인 계정 + 샘플 프로젝트/이슈
-- 비밀번호(공통): dev123 (BCrypt, strength 12, Spring Security와 동일)
-- 로그인 식별자: email
--
-- 계정 요약 (프로젝트 권한은 project_member_tb):
--   dev@local.test      — DEMO 프로젝트 ADMIN (기본 개발자)
--   admin@local.test    — DEMO 프로젝트 ADMIN (관리자 시나리오 전용 테스트)
--   viewer@local.test   — DEMO 프로젝트 VIEWER

INSERT INTO user_account_tb (id, email, password, name, created_at, updated_at) VALUES
(1, 'dev@local.test', '$2b$12$AVeWW4hwpcqEYXl3mKMiXO1kuy9jqEUAmy27iEWuodD0IqPX0Qk3e', 'Dev User', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'viewer@local.test', '$2b$12$AVeWW4hwpcqEYXl3mKMiXO1kuy9jqEUAmy27iEWuodD0IqPX0Qk3e', 'Viewer User', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'admin@local.test', '$2b$12$AVeWW4hwpcqEYXl3mKMiXO1kuy9jqEUAmy27iEWuodD0IqPX0Qk3e', 'Admin Test', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO project_tb (id, archived, board_type, description, issue_sequence, auto_archive_done_after_days, "key", name, lead_id, created_at, updated_at) VALUES
(1, FALSE, 'SCRUM', '로컬 개발용 샘플 프로젝트', 2, NULL, 'DEMO', 'Demo Project', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO project_member_tb (id, project_id, user_id, role, joined_at) VALUES
(1, 1, 1, 'ADMIN', CURRENT_TIMESTAMP),
(2, 1, 2, 'VIEWER', CURRENT_TIMESTAMP),
(3, 1, 3, 'ADMIN', CURRENT_TIMESTAMP);

INSERT INTO sprint_tb (id, project_id, name, status, start_date, end_date, goal_points, created_at) VALUES
(1, 1, 'Sprint 1', 'ACTIVE', CURRENT_DATE, DATEADD('DAY', 14, CURRENT_DATE), NULL, CURRENT_TIMESTAMP);

INSERT INTO issue_tb (id, issue_key, project_id, issue_type, summary, description, status, priority, reporter_id, assignee_id, backlog_rank, story_points, sprint_id, parent_id, security_level, epic_start_date, epic_end_date, archived, created_at, updated_at) VALUES
(1, 'DEMO-1', 1, 'STORY', '샘플 스토리', 'H2 dev 시드 이슈', 'IN_PROGRESS', 'HIGH', 1, 1, 1000, 3, 1, NULL, NULL, NULL, NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'DEMO-2', 1, 'BUG', '샘플 버그', '백로그 상태 샘플', 'BACKLOG', 'MEDIUM', 1, NULL, 2000, NULL, NULL, NULL, NULL, NULL, NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

ALTER TABLE user_account_tb ALTER COLUMN id RESTART WITH 100;
ALTER TABLE project_tb ALTER COLUMN id RESTART WITH 100;
ALTER TABLE project_member_tb ALTER COLUMN id RESTART WITH 100;
ALTER TABLE sprint_tb ALTER COLUMN id RESTART WITH 100;
ALTER TABLE issue_tb ALTER COLUMN id RESTART WITH 100;
