-- Migration: Rename hall_model to user_credential (Requirement #4)
-- Run once on existing databases that have hall_model. Safe to run: no-ops if hall_model does not exist.

USE hall_ticket;

-- Only rename if hall_model exists (avoids error on fresh installs)
SET @rename_sql = (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'hall_ticket' AND table_name = 'hall_model') > 0,
    'RENAME TABLE hall_model TO user_credential',
    'SELECT 1'
  )
);
PREPARE stmt FROM @rename_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
