package me.leoko.advancedban.bukkit.listener;

import me.leoko.advancedban.bukkit.discord.DiscordWebhookManager;
import me.leoko.advancedban.bukkit.event.PunishmentEvent;
import me.leoko.advancedban.bukkit.event.RevokePunishmentEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class InternalListener implements Listener {
    
    @EventHandler
    public void onPunish(PunishmentEvent e) {
        if (e.getPunishment() != null) {
            DiscordWebhookManager.get().sendPunishment(e.getPunishment());
        }
    }
    
    @EventHandler
    public void onRevokePunishment(RevokePunishmentEvent e) {
        if (e.getPunishment() != null) {
            DiscordWebhookManager.get().sendRevoke(e.getPunishment(), "Staff / Console");
        }
    }
}
