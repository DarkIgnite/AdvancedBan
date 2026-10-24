package me.leoko.advancedban.manager;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.utils.DetectedAlt;
import me.leoko.advancedban.utils.DetectionKind;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.SQLQuery;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Manages player IP addresses and alt account detection with LibertyBans-style logic.
 */
public class AltManager {
    private static AltManager instance = null;

    public static synchronized AltManager get() {
        return instance == null ? instance = new AltManager() : instance;
    }

    public void setup() {
        DatabaseManager.get().executeStatement(SQLQuery.CREATE_TABLE_ADDRESSES);
    }

    /**
     * Record a player's connection address and timestamp.
     */
    public void recordConnection(String uuid, String name, String ip) {
        if (uuid == null || ip == null || name == null) return;
        long now = System.currentTimeMillis();
        Universal.get().getMethods().runAsync(() -> {
            try {
                ResultSet rs = DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_EXACT_ADDRESS, uuid, ip);
                if (rs != null && rs.next()) {
                    DatabaseManager.get().executeStatement(SQLQuery.UPDATE_ADDRESS, name, now, uuid, ip);
                } else {
                    DatabaseManager.get().executeStatement(SQLQuery.INSERT_ADDRESS, uuid, name, ip, now);
                }
            } catch (SQLException e) {
                Universal.get().debugSqlException(e);
            }
        });
    }

    /**
     * Detect alt accounts for the given target (uuid, name, or ip).
     */
    public List<DetectedAlt> detectAlts(String targetUuid, String targetName, String targetIp) {
        if (targetUuid == null && targetName != null) {
            targetUuid = UUIDManager.get().getUUID(targetName);
        }
        if (targetIp == null && targetName != null) {
            targetIp = Universal.get().getIps().get(targetName.toLowerCase());
        }

        // If targetUuid is null and targetIp is not, search directly by IP
        if (targetUuid == null) {
            if (targetIp == null) return Collections.emptyList();
            return detectAltsByIp(targetIp, null);
        }

        // 1. Get latest IP of target to compare for NORMAL vs STRICT
        if (targetIp == null) {
            try {
                ResultSet rsLatest = DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_LATEST_ADDRESS_BY_UUID, targetUuid);
                if (rsLatest != null && rsLatest.next()) {
                    targetIp = rsLatest.getString("ip");
                }
            } catch (SQLException e) {
                Universal.get().debugSqlException(e);
            }
        }

        // 2. Fetch all raw alt records from the database
        Map<String, DetectedAltRaw> bestAltsByUuid = new HashMap<>();

        try {
            ResultSet rs = DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_ALTS_BY_UUID, targetUuid, targetUuid);
            if (rs != null) {
                while (rs.next()) {
                    String altUuid = rs.getString("uuid");
                    String altName = rs.getString("name");
                    String altIp = rs.getString("ip");
                    long lastSeen = rs.getLong("last_seen");

                    boolean isNormal = targetIp != null && targetIp.equalsIgnoreCase(altIp);
                    DetectionKind kind = isNormal ? DetectionKind.NORMAL : DetectionKind.STRICT;

                    DetectedAltRaw existing = bestAltsByUuid.get(altUuid);
                    if (existing == null) {
                        bestAltsByUuid.put(altUuid, new DetectedAltRaw(altUuid, altName, altIp, lastSeen, kind));
                    } else {
                        // Prefer NORMAL detection over STRICT, or newer lastSeen
                        if (existing.kind == DetectionKind.STRICT && kind == DetectionKind.NORMAL) {
                            bestAltsByUuid.put(altUuid, new DetectedAltRaw(altUuid, altName, altIp, Math.max(lastSeen, existing.lastSeen), kind));
                        } else if (lastSeen > existing.lastSeen) {
                            bestAltsByUuid.put(altUuid, new DetectedAltRaw(altUuid, altName, altIp, lastSeen, existing.kind == DetectionKind.NORMAL ? DetectionKind.NORMAL : kind));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Universal.get().debugSqlException(e);
        }

        // If no alts in table but target has memory IP, fallback to search by IP
        if (bestAltsByUuid.isEmpty() && targetIp != null) {
            return detectAltsByIp(targetIp, targetUuid);
        }

        // 3. For each unique alt, query active punishments
        List<DetectedAlt> results = new ArrayList<>();
        for (DetectedAltRaw raw : bestAltsByUuid.values()) {
            Punishment active = PunishmentManager.get().getBan(raw.uuid);
            if (active == null) {
                active = PunishmentManager.get().getMute(raw.uuid);
            }
            results.add(new DetectedAlt(raw.uuid, raw.name, raw.ip, raw.lastSeen, raw.kind, active));
        }

        // 4. Sort results by last seen (newest first)
        results.sort((a, b) -> Long.compare(b.getLastSeen(), a.getLastSeen()));

        return results;
    }

    private List<DetectedAlt> detectAltsByIp(String ip, String excludeUuid) {
        Map<String, DetectedAltRaw> bestAltsByUuid = new HashMap<>();
        try {
            ResultSet rs = DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_ALTS_BY_IP, ip, excludeUuid == null ? "" : excludeUuid);
            if (rs != null) {
                while (rs.next()) {
                    String altUuid = rs.getString("uuid");
                    String altName = rs.getString("name");
                    String altIp = rs.getString("ip");
                    long lastSeen = rs.getLong("last_seen");
                    bestAltsByUuid.put(altUuid, new DetectedAltRaw(altUuid, altName, altIp, lastSeen, DetectionKind.NORMAL));
                }
            }
        } catch (SQLException e) {
            Universal.get().debugSqlException(e);
        }

        List<DetectedAlt> results = new ArrayList<>();
        for (DetectedAltRaw raw : bestAltsByUuid.values()) {
            Punishment active = PunishmentManager.get().getBan(raw.uuid);
            if (active == null) {
                active = PunishmentManager.get().getMute(raw.uuid);
            }
            results.add(new DetectedAlt(raw.uuid, raw.name, raw.ip, raw.lastSeen, raw.kind, active));
        }
        results.sort((a, b) -> Long.compare(b.getLastSeen(), a.getLastSeen()));
        return results;
    }

    public int getAltsCount(String targetUuid, String targetName, String targetIp) {
        return detectAlts(targetUuid, targetName, targetIp).size();
    }

    public String getLatestAddress(String uuid) {
        if (uuid == null) return null;
        try {
            ResultSet rs = DatabaseManager.get().executeResultStatement(SQLQuery.SELECT_LATEST_ADDRESS_BY_UUID, uuid);
            if (rs != null && rs.next()) {
                return rs.getString("ip");
            }
        } catch (SQLException e) {
            Universal.get().debugSqlException(e);
        }
        return null;
    }

    /**
     * Check and alert online staff when a player joins with alts.
     */
    public void checkAndNotifyOnJoin(String name, String uuid, String ip) {
        MethodInterface mi = Universal.get().getMethods();
        if (!mi.getBoolean(mi.getConfig(), "Alts.AlertOnJoin", true)) return;

        mi.scheduleAsync(() -> {
            List<DetectedAlt> alts = detectAlts(uuid, name, ip);
            if (alts.isEmpty()) return;

            // Only notify if there are alts (or if configured to only notify on punished alts)
            boolean onlyPunished = mi.getBoolean(mi.getConfig(), "Alts.AlertOnlyPunished", false);
            if (onlyPunished && alts.stream().noneMatch(DetectedAlt::hasActivePunishment)) {
                return;
            }

            int limit = mi.getInteger(mi.getConfig(), "Alts.AlertLimit", 5);
            String prefix = MessageManager.getMessage("General.Prefix");
            List<String> header = MessageManager.getLayout(mi.getMessages(), "Alts.AlertOnJoin.Header", "PREFIX", prefix, "TARGET", name, "COUNT", String.valueOf(alts.size()));

            List<String> entries = new ArrayList<>();
            for (int i = 0; i < Math.min(alts.size(), limit); i++) {
                DetectedAlt alt = alts.get(i);
                String userFormatted = formatUserName(alt);
                String kindFormatted = formatDetectionKind(alt.getDetectionKind());
                String entry = MessageManager.getMessage("Alts.AlertOnJoin.Entry",
                        "RELEVANT_USER", userFormatted,
                        "DETECTION_KIND", kindFormatted,
                        "ADDRESS", alt.getIp(),
                        "USERNAME", alt.getName());
                entries.add(entry);
            }

            // Broadcast to online players with permission ab.alts.notify or ab.alts
            for (Object player : mi.getOnlinePlayers()) {
                if (Universal.get().hasPerms(player, "ab.alts.notify") || Universal.get().hasPerms(player, "ab.alts")) {
                    for (String h : header) {
                        mi.sendMessage(player, h);
                    }
                    for (String e : entries) {
                        mi.sendMessage(player, e);
                    }
                }
            }
        }, 40);
    }

    public static String formatUserName(DetectedAlt alt) {
        if (alt.isBanned()) {
            return MessageManager.getMessage("Alts.Formatting.NameDisplay.Banned", "USERNAME", alt.getName());
        } else if (alt.isMuted()) {
            return MessageManager.getMessage("Alts.Formatting.NameDisplay.Muted", "USERNAME", alt.getName());
        } else {
            return MessageManager.getMessage("Alts.Formatting.NameDisplay.NotPunished", "USERNAME", alt.getName());
        }
    }

    public static String formatDetectionKind(DetectionKind kind) {
        if (kind == DetectionKind.NORMAL) {
            return MessageManager.getMessage("Alts.Formatting.Normal");
        } else {
            return MessageManager.getMessage("Alts.Formatting.Strict");
        }
    }

    private static class DetectedAltRaw {
        final String uuid;
        final String name;
        final String ip;
        final long lastSeen;
        final DetectionKind kind;

        DetectedAltRaw(String uuid, String name, String ip, long lastSeen, DetectionKind kind) {
            this.uuid = uuid;
            this.name = name;
            this.ip = ip;
            this.lastSeen = lastSeen;
            this.kind = kind;
        }
    }
}
