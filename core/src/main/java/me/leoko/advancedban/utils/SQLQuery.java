package me.leoko.advancedban.utils;

import me.leoko.advancedban.manager.DatabaseManager;

/**
 * Created by Leo on 29.07.2017.
 */
public enum SQLQuery {
    CREATE_TABLE_PUNISHMENT(
            "CREATE TABLE IF NOT EXISTS `Punishments` ("+
            "`id` int NOT NULL AUTO_INCREMENT," +
            "`name` VARCHAR(16) NULL DEFAULT NULL," +
            "`uuid` VARCHAR(35) NULL DEFAULT NULL," +
            "`reason` VARCHAR(255) NULL DEFAULT NULL," +
            "`operator` VARCHAR(16) NULL DEFAULT NULL," +
            "`punishmentType` VARCHAR(16) NULL DEFAULT NULL," +
            "`start` LONG DEFAULT NULL," +
            "`end` LONG DEFAULT NULL," +
            "`calculation` VARCHAR(50) NULL DEFAULT NULL," +
            "PRIMARY KEY (`id`))",

            "CREATE TABLE IF NOT EXISTS Punishments (" +
            "id INTEGER IDENTITY PRIMARY KEY," +
            "name VARCHAR(16)," +
            "uuid VARCHAR(35)," +
            "reason VARCHAR(255)," +
            "operator VARCHAR(16)," +
            "punishmentType VARCHAR(16)," +
            "start BIGINT," +
            "end BIGINT," +
            "calculation VARCHAR(50))"
    ),
    CREATE_TABLE_PUNISHMENT_HISTORY(
            "CREATE TABLE IF NOT EXISTS `PunishmentHistory` (" +
            "`id` int NOT NULL AUTO_INCREMENT," +
            "`name` VARCHAR(16) NULL DEFAULT NULL," +
            "`uuid` VARCHAR(35) NULL DEFAULT NULL," +
            "`reason` VARCHAR(255) NULL DEFAULT NULL," +
            "`operator` VARCHAR(16) NULL DEFAULT NULL," +
            "`punishmentType` VARCHAR(16) NULL DEFAULT NULL," +
            "`start` LONG DEFAULT NULL," +
            "`end` LONG DEFAULT NULL," +
            "`calculation` VARCHAR(50) NULL DEFAULT NULL," +
            "PRIMARY KEY (`id`))",

            "CREATE TABLE IF NOT EXISTS PunishmentHistory (" +
            "id INTEGER IDENTITY PRIMARY KEY," +
            "name VARCHAR(16)," +
            "uuid VARCHAR(35)," +
            "reason VARCHAR(255)," +
            "operator VARCHAR(16)," +
            "punishmentType VARCHAR(16)," +
            "start BIGINT," +
            "end BIGINT," +
            "calculation VARCHAR(50))"
    ),
    INSERT_PUNISHMENT(
            "INSERT INTO `Punishments` " +
            "(`name`, `uuid`, `reason`, `operator`, `punishmentType`, `start`, `end`, `calculation`) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",

            "INSERT INTO Punishments " +
            "(name, uuid, reason, operator, punishmentType, start, end, calculation) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
    ),
    INSERT_PUNISHMENT_HISTORY(
            "INSERT INTO `PunishmentHistory` " +
            "(`name`, `uuid`, `reason`, `operator`, `punishmentType`, `start`, `end`, `calculation`) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",

            "INSERT INTO PunishmentHistory " +
            "(name, uuid, reason, operator, punishmentType, start, end, calculation) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
    ),
    SELECT_EXACT_PUNISHMENT(
            "SELECT * FROM `Punishments` WHERE `uuid` = ? AND `start` = ? AND `punishmentType` = ?",
            "SELECT * FROM Punishments WHERE uuid = ? AND start = ? AND punishmentType = ?"
    ),
    DELETE_PUNISHMENT(
            "DELETE FROM `Punishments` WHERE `id` = ?",
            "DELETE FROM Punishments WHERE id = ?"
    ),
    DELETE_OLD_PUNISHMENTS(
            "DELETE FROM `Punishments` WHERE `end` <= ? AND `end` != -1",
            "DELETE FROM Punishments WHERE end <= ? AND end != -1"
    ),
    SELECT_USER_PUNISHMENTS(
            "SELECT * FROM `Punishments` WHERE `uuid` = ? OR LOWER(`name`) = LOWER(?)",
            "SELECT * FROM Punishments WHERE uuid = ? OR LOWER(name) = LOWER(?)"
    ),
    SELECT_USER_PUNISHMENTS_HISTORY(
            "SELECT * FROM `PunishmentHistory` WHERE `uuid` = ? OR LOWER(`name`) = LOWER(?)",
            "SELECT * FROM PunishmentHistory WHERE uuid = ? OR LOWER(name) = LOWER(?)"
    ),
    SELECT_USER_PUNISHMENTS_WITH_IP(
            "SELECT * FROM `Punishments` WHERE `uuid` = ? OR `uuid` = ?",
            "SELECT * FROM Punishments WHERE uuid = ? OR uuid = ?"
    ),
    SELECT_USER_PUNISHMENTS_HISTORY_WITH_IP(
            "SELECT * FROM `PunishmentHistory` WHERE `uuid` = ? OR `uuid` = ?",
            "SELECT * FROM PunishmentHistory WHERE uuid = ? OR uuid = ?"
    ),
    SELECT_USER_PUNISHMENTS_HISTORY_BY_CALCULATION(
            "SELECT * FROM `PunishmentHistory` WHERE `uuid` = ? AND `calculation` = ?",
            "SELECT * FROM PunishmentHistory WHERE uuid = ? AND calculation = ?"
    ),
    UPDATE_PUNISHMENT_REASON(
            "UPDATE `Punishments` SET `reason` = ? WHERE `id` = ?",
            "UPDATE Punishments SET reason = ? WHERE id = ?"
    ),
    SELECT_PUNISHMENT_BY_ID(
            "SELECT * FROM `Punishments` WHERE `id` = ?",
            "SELECT * FROM Punishments WHERE id = ?"
    ),
    SELECT_ALL_PUNISHMENTS(
            "SELECT * FROM `Punishments`",
            "SELECT * FROM Punishments"
    ),
    SELECT_ALL_PUNISHMENTS_ORDER_BY_ID_DESC(
            "SELECT * FROM `Punishments` ORDER BY `id` DESC",
            "SELECT * FROM Punishments ORDER BY id DESC"
    ),
    DELETE_USER_PUNISHMENTS_BY_BASIC_TYPE(
            "DELETE FROM `Punishments` WHERE (`uuid` = ? OR LOWER(`name`) = LOWER(?)) AND (`punishmentType` = ? OR `punishmentType` = ?)",
            "DELETE FROM Punishments WHERE (uuid = ? OR LOWER(name) = LOWER(?)) AND (punishmentType = ? OR punishmentType = ?)"
    ),
    SELECT_ALL_PUNISHMENTS_HISTORY(
            "SELECT * FROM `PunishmentHistory`",
            "SELECT * FROM PunishmentHistory"
    ),
    SELECT_ALL_PUNISHMENTS_LIMIT(
            "SELECT * FROM `Punishments` ORDER BY `start` DESC LIMIT ?",
            "SELECT * FROM Punishments ORDER BY start DESC LIMIT ?"
    ),
    SELECT_ALL_PUNISHMENTS_HISTORY_LIMIT(
            "SELECT * FROM `PunishmentHistory` ORDER BY `start` DESC LIMIT ?",
            "SELECT * FROM PunishmentHistory ORDER BY start DESC LIMIT ?"
    ),
    CREATE_TABLE_ADDRESSES(
            "CREATE TABLE IF NOT EXISTS `Addresses` (" +
            "`id` int NOT NULL AUTO_INCREMENT," +
            "`uuid` VARCHAR(36) NOT NULL," +
            "`name` VARCHAR(16) NOT NULL," +
            "`ip` VARCHAR(45) NOT NULL," +
            "`last_seen` BIGINT NOT NULL," +
            "PRIMARY KEY (`id`)," +
            "KEY `idx_uuid` (`uuid`)," +
            "KEY `idx_ip` (`ip`))",

            "CREATE TABLE IF NOT EXISTS Addresses (" +
            "id INTEGER IDENTITY PRIMARY KEY," +
            "uuid VARCHAR(36)," +
            "name VARCHAR(16)," +
            "ip VARCHAR(45)," +
            "last_seen BIGINT)"
    ),
    SELECT_EXACT_ADDRESS(
            "SELECT * FROM `Addresses` WHERE `uuid` = ? AND `ip` = ?",
            "SELECT * FROM Addresses WHERE uuid = ? AND ip = ?"
    ),
    INSERT_ADDRESS(
            "INSERT INTO `Addresses` (`uuid`, `name`, `ip`, `last_seen`) VALUES (?, ?, ?, ?)",
            "INSERT INTO Addresses (uuid, name, ip, last_seen) VALUES (?, ?, ?, ?)"
    ),
    UPDATE_ADDRESS(
            "UPDATE `Addresses` SET `name` = ?, `last_seen` = ? WHERE `uuid` = ? AND `ip` = ?",
            "UPDATE Addresses SET name = ?, last_seen = ? WHERE uuid = ? AND ip = ?"
    ),
    SELECT_LATEST_ADDRESS_BY_UUID(
            "SELECT `ip` FROM `Addresses` WHERE `uuid` = ? ORDER BY `last_seen` DESC",
            "SELECT ip FROM Addresses WHERE uuid = ? ORDER BY last_seen DESC"
    ),
    SELECT_ALTS_BY_UUID(
            "SELECT a.* FROM `Addresses` a WHERE a.`ip` IN (SELECT DISTINCT b.`ip` FROM `Addresses` b WHERE b.`uuid` = ?) AND a.`uuid` != ? ORDER BY a.`last_seen` DESC",
            "SELECT a.id, a.uuid, a.name, a.ip, a.last_seen FROM Addresses a WHERE a.ip IN (SELECT DISTINCT b.ip FROM Addresses b WHERE b.uuid = ?) AND a.uuid != ? ORDER BY a.last_seen DESC"
    ),
    SELECT_ALTS_BY_IP(
            "SELECT * FROM `Addresses` WHERE `ip` = ? AND `uuid` != ? ORDER BY `last_seen` DESC",
            "SELECT * FROM Addresses WHERE ip = ? AND uuid != ? ORDER BY last_seen DESC"
    ),
    DELETE_ADDRESS_BY_UUID(
            "DELETE FROM `Addresses` WHERE `uuid` = ?",
            "DELETE FROM Addresses WHERE uuid = ?"
    );

    private String mysql;
    private String hsqldb;

    SQLQuery(String mysql, String hsqldb) {
        this.mysql = mysql;
        this.hsqldb = hsqldb;
    }

    @Override
    public String toString() {
        return DatabaseManager.get().isUseMySQL() ? mysql : hsqldb;
    }
}
