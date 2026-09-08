package me.leoko.advancedban.utils;

/**
 * Represents a detected alt account matching LibertyBans structure.
 */
public class DetectedAlt {
    private final String uuid;
    private final String name;
    private final String ip;
    private final long lastSeen;
    private final DetectionKind detectionKind;
    private final Punishment activePunishment;

    public DetectedAlt(String uuid, String name, String ip, long lastSeen, DetectionKind detectionKind, Punishment activePunishment) {
        this.uuid = uuid;
        this.name = name;
        this.ip = ip;
        this.lastSeen = lastSeen;
        this.detectionKind = detectionKind;
        this.activePunishment = activePunishment;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public DetectionKind getDetectionKind() {
        return detectionKind;
    }

    public Punishment getActivePunishment() {
        return activePunishment;
    }

    public boolean isBanned() {
        return activePunishment != null && (activePunishment.getType().isBan() || activePunishment.getType().isTemp());
    }

    public boolean isMuted() {
        return activePunishment != null && activePunishment.getType().isMute();
    }
}
