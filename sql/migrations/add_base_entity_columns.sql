-- Run on an existing ELADMIN database before deploying the updated entities.
-- Existing columns and values are preserved. This migration can be rerun.
DROP PROCEDURE IF EXISTS eladmin_migrate_base_entity_columns;

DELIMITER //
CREATE PROCEDURE eladmin_migrate_base_entity_columns(IN table_name_to_update VARCHAR(64))
BEGIN
    DECLARE additions TEXT;

    SELECT GROUP_CONCAT(required_column.ddl ORDER BY required_column.column_name SEPARATOR ', ')
    INTO additions
    FROM (
        SELECT 'enabled' AS column_name, 'ADD COLUMN `enabled` bit(1) DEFAULT NULL COMMENT ''是否启用''' AS ddl
        UNION ALL SELECT 'sort', 'ADD COLUMN `sort` int DEFAULT NULL COMMENT ''排序'''
        UNION ALL SELECT 'remark', 'ADD COLUMN `remark` varchar(255) DEFAULT NULL COMMENT ''备注'''
    ) required_column
    WHERE NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS c
        WHERE c.TABLE_SCHEMA = DATABASE()
          AND c.TABLE_NAME = table_name_to_update
          AND c.COLUMN_NAME = required_column.column_name
    );

    -- The scheduler table exceeds the COMPACT row size limit with the new columns.
    IF table_name_to_update = 'sys_quartz_job' AND EXISTS (
        SELECT 1
        FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = table_name_to_update AND ROW_FORMAT <> 'Dynamic'
    ) THEN
        SET additions = CONCAT_WS(', ', additions, 'ROW_FORMAT=DYNAMIC');
    END IF;

    IF additions IS NOT NULL THEN
        SET @eladmin_base_entity_ddl = CONCAT('ALTER TABLE `', table_name_to_update, '` ', additions);
        PREPARE eladmin_base_entity_ddl FROM @eladmin_base_entity_ddl;
        EXECUTE eladmin_base_entity_ddl;
        DEALLOCATE PREPARE eladmin_base_entity_ddl;
    END IF;
END//
DELIMITER ;

CALL eladmin_migrate_base_entity_columns('mnt_app');
CALL eladmin_migrate_base_entity_columns('mnt_database');
CALL eladmin_migrate_base_entity_columns('mnt_deploy');
CALL eladmin_migrate_base_entity_columns('mnt_server');
CALL eladmin_migrate_base_entity_columns('sys_dept');
CALL eladmin_migrate_base_entity_columns('sys_dict');
CALL eladmin_migrate_base_entity_columns('sys_dict_detail');
CALL eladmin_migrate_base_entity_columns('sys_job');
CALL eladmin_migrate_base_entity_columns('sys_menu');
CALL eladmin_migrate_base_entity_columns('sys_quartz_job');
CALL eladmin_migrate_base_entity_columns('sys_role');
CALL eladmin_migrate_base_entity_columns('sys_user');
CALL eladmin_migrate_base_entity_columns('tool_local_storage');
CALL eladmin_migrate_base_entity_columns('tool_s3_storage');

DROP PROCEDURE eladmin_migrate_base_entity_columns;
