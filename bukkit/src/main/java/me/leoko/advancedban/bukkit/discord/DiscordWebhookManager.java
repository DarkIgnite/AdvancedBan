package me.leoko.advancedban.bukkit.discord;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.bukkit.BukkitMain;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiscordWebhookManager {

    private static DiscordWebhookManager instance;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static synchronized DiscordWebhookManager get() {
        if (instance == null) {
            instance = new DiscordWebhookManager();
        }
        return instance;
    }

    private FileConfiguration getConfig() {
        return BukkitMain.get().getConfig();
    }

    public boolean isEnabled() {
        return getConfig().getBoolean("Discord.Enabled", true);
    }

    public String getWebhookUrl() {
        return getConfig().getString("Discord.WebhookURL", "");
    }

    public void sendPunishment(Punishment punishment) {
        if (!isEnabled()) return;
        String webhookUrl = getWebhookUrl();
        if (webhookUrl == null || webhookUrl.trim().isEmpty() || !webhookUrl.startsWith("http")) return;

        executor.submit(() -> {
            try {
                PunishmentType type = punishment.getType();
                String typeName = type.getName().toUpperCase();
                String title;
                int color;

                switch (type.getBasic()) {
                    case BAN -> {
                        title = type.isTemp() ? "⏳ TEMPBAN" : "🔨 BANNED";
                        color = type.isTemp() ? 16737792 : 16711680;
                    }
                    case MUTE -> {
                        title = type.isTemp() ? "⏳ TEMPMUTE" : "🔇 MUTED";
                        color = type.isTemp() ? 16763904 : 16753920;
                    }
                    case WARNING -> {
                        title = type.isTemp() ? "⏳ TEMP WARN" : "⚠️ WARNED";
                        color = 16776960;
                    }
                    case KICK -> {
                        title = "👢 KICKED";
                        color = 10038562;
                    }
                    case JAIL -> {
                        title = type.isTemp() ? "⏳ TEMP JAIL" : "⛓️ JAILED";
                        color = 6323595;
                    }
                    default -> {
                        title = "⚖️ " + typeName;
                        color = 3447003;
                    }
                }

                String duration = type.isTemp() ? punishment.getDuration(true) : "Permanen";
                String target = punishment.getName() != null ? punishment.getName() : punishment.getUuid();
                String operator = punishment.getOperator() != null ? punishment.getOperator() : "Console";
                String reason = punishment.getReason() != null ? punishment.getReason() : "Tidak ada alasan";
                String avatarUrl = "https://mc-heads.net/avatar/" + target + "/100";

                String payload = "{"
                        + "\"embeds\": [{"
                        + "\"title\": \"" + escapeJson(title) + "\","
                        + "\"color\": " + color + ","
                        + "\"thumbnail\": {\"url\": \"" + avatarUrl + "\"},"
                        + "\"fields\": ["
                        + "{\"name\": \"👤 Pemain\", \"value\": \"`" + escapeJson(target) + "`\", \"inline\": true},"
                        + "{\"name\": \"🛡️ Staff / Petugas\", \"value\": \"`" + escapeJson(operator) + "`\", \"inline\": true},"
                        + "{\"name\": \"⏱️ Durasi\", \"value\": \"`" + escapeJson(duration) + "`\", \"inline\": true},"
                        + "{\"name\": \"📝 Alasan\", \"value\": \"" + escapeJson(reason) + "\", \"inline\": false}"
                        + "],"
                        + "\"footer\": {\"text\": \"Survival Season 7 • AdvancedBan\"},"
                        + "\"timestamp\": \"" + Instant.now().toString() + "\""
                        + "}]"
                        + "}";

                postJson(webhookUrl, payload);
            } catch (Throwable t) {
                BukkitMain.get().getLogger().warning("Failed to dispatch Discord punishment webhook: " + t.getMessage());
            }
        });
    }

    public void sendRevoke(Punishment punishment, String operator) {
        if (!isEnabled()) return;
        String webhookUrl = getWebhookUrl();
        if (webhookUrl == null || webhookUrl.trim().isEmpty() || !webhookUrl.startsWith("http")) return;

        executor.submit(() -> {
            try {
                String typeName = punishment.getType().getBasic().getName().toUpperCase();
                String target = punishment.getName() != null ? punishment.getName() : punishment.getUuid();
                String op = operator != null ? operator : "Console";
                String avatarUrl = "https://mc-heads.net/avatar/" + target + "/100";

                String payload = "{"
                        + "\"embeds\": [{"
                        + "\"title\": \"🕊️ SANKSI DICABUT: UN" + escapeJson(typeName) + "\","
                        + "\"color\": 65280,"
                        + "\"thumbnail\": {\"url\": \"" + avatarUrl + "\"},"
                        + "\"fields\": ["
                        + "{\"name\": \"👤 Pemain\", \"value\": \"`" + escapeJson(target) + "`\", \"inline\": true},"
                        + "{\"name\": \"🛡️ Staff / Petugas\", \"value\": \"`" + escapeJson(op) + "`\", \"inline\": true},"
                        + "{\"name\": \"⚖️ Sanksi Sebelumnya\", \"value\": \"`" + escapeJson(punishment.getType().getName()) + "`\", \"inline\": true}"
                        + "],"
                        + "\"footer\": {\"text\": \"Survival Season 7 • AdvancedBan\"},"
                        + "\"timestamp\": \"" + Instant.now().toString() + "\""
                        + "}]"
                        + "}";

                postJson(webhookUrl, payload);
            } catch (Throwable t) {
                BukkitMain.get().getLogger().warning("Failed to dispatch Discord revoke webhook: " + t.getMessage());
            }
        });
    }

    private void postJson(String webhookUrl, String json) throws Exception {
        URL url = URI.create(webhookUrl).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("User-Agent", "AdvancedBanReworked-DiscordHook/1.0");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            BukkitMain.get().getLogger().warning("Discord webhook returned response code: " + responseCode);
        }
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
