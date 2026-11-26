-- Run once on an existing ELADMIN database before deploying the updated menu service.
SET @menu_title_migration = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_menu' AND INDEX_NAME = 'uniq_title'
    ),
    'ALTER TABLE sys_menu DROP INDEX uniq_title',
    'DO 0'
);
PREPARE menu_title_migration FROM @menu_title_migration;
EXECUTE menu_title_migration;
DEALLOCATE PREPARE menu_title_migration;
