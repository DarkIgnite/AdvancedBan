package me.leoko.advancedban.utils;

/**
 * Created by Leoko @ dev.skamps.eu on 30.05.2016.
 */
public enum PunishmentType {
    BAN("Ban", null, false, "ab.ban.perma"),
    TEMP_BAN("Tempban", BAN, true, "ab.ban.temp"),
    IP_BAN("Ipban", BAN, false, "ab.ipban.perma"),
    TEMP_IP_BAN("Tempipban", BAN, true, "ab.ipban.temp"),
    MUTE("Mute", null, false, "ab.mute.perma"),
    TEMP_MUTE("Tempmute", MUTE, true, "ab.mute.temp"),
    WARNING("Warn", null, false, "ab.warn.perma"),
    TEMP_WARNING("Tempwarn", WARNING, true, "ab.warn.temp"),
    KICK("Kick", null, false, "ab.kick.use"),
    NOTE("Note", null, false, "ab.note.use"),
    JAIL("Jail", null, false, "ab.jail.perma"),
    TEMP_JAIL("Tempjail", JAIL, true, "ab.jail.temp");

    private final String name;
    private final String perms;
    private final PunishmentType basic;
    private final boolean temp;

    PunishmentType(String name, PunishmentType basic, boolean temp, String perms) {
        this.name = name;
        this.basic = basic;
        this.temp = temp;
        this.perms = perms;
    }

    public static PunishmentType fromCommandName(String cmd) {
        switch (cmd.toLowerCase()) {
            case "ban":
                return BAN;
            case "tempban":
                return TEMP_BAN;
            case "ban-ip":
            case "banip":
            case "ipban":
                return IP_BAN;
            case "tempipban":
            case "tipban":
                return TEMP_IP_BAN;
            case "mute":
                return MUTE;
            case "tempmute":
                return TEMP_MUTE;
            case "warn":
                return WARNING;
            case "note":
                return NOTE;
            case "tempwarn":
                return TEMP_WARNING;
            case "kick":
                return KICK;
            case "jail":
                return JAIL;
            case "tempjail":
                return TEMP_JAIL;
            default:
                return null;
        }
    }

    public String getName() {
        return name;
    }

    public String getPerms() {
        return perms;
    }

    public boolean isTemp() {
        return temp;
    }

    public String getConfSection(String path) {
        return name + "." + path;
    }

    public PunishmentType getBasic() {
        return basic == null ? this : basic;
    }

    public PunishmentType getPermanent() {
        if (this == IP_BAN || this == TEMP_IP_BAN)
            return IP_BAN;

        return getBasic();
    }

    public boolean isIpOrientated() {
        return this == IP_BAN || this == TEMP_IP_BAN;
    }

    public boolean isBan() {
        return getBasic() == BAN;
    }

    public boolean isMute() {
        return getBasic() == MUTE;
    }

    public boolean isWarning() {
        return getBasic() == WARNING;
    }

    public boolean isJail() {
        return getBasic() == JAIL;
    }
}
