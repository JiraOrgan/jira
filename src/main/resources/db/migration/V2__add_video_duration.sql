-- Add video_duration (seconds) to lesson_tb for precise video length tracking
ALTER TABLE lesson_tb ADD COLUMN video_duration INT NULL COMMENT 'Video duration in seconds';
