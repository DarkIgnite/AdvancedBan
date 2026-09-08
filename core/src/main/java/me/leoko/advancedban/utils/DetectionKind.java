package me.leoko.advancedban.utils;

/**
 * Indicates the possibility level / kind of alt detection.
 * Matches LibertyBans detection classification:
 * - NORMAL: Strong possibility (Shares the target's current/latest IP address)
 * - STRICT: Mere possibility (Shares a past historical IP address)
 */
public enum DetectionKind {
    NORMAL,
    STRICT
}
