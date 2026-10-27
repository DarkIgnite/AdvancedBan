package me.leoko.advancedban.bukkit.listener;

import me.leoko.advancedban.bukkit.event.PunishmentEvent;
import me.leoko.advancedban.bukkit.event.RevokePunishmentEvent;
import me.leoko.advancedban.utils.PunishmentType;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Date;

/**
 * @deprecated Disabled in AdvancedBanReworked: Ban synchronization to vanilla banned-players.json
 * causes unban failures and conflicts with modern Paper/Bedrock servers. Bans are now exclusively
 * managed by AdvancedBan's database and pre-login listeners.
 *
 * @author Beelzebu
 */
@Deprecated
public class InternalListener implements Listener {
    
    @EventHandler
    public void onPunish(PunishmentEvent e) {
        // Disabled: do not sync to vanilla BanList
    }
    
    @EventHandler
    public void onRevokePunishment(RevokePunishmentEvent e) {
        // Disabled: do not sync to vanilla BanList
    }
}
