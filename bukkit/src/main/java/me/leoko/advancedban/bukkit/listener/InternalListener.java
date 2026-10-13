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
 *
 * @author Beelzebu
 */
public class InternalListener implements Listener {
    
    @EventHandler
    public void onPunish(PunishmentEvent e) {
        try {
            BanList banlist;
            if (e.getPunishment().getType().equals(PunishmentType.BAN) || e.getPunishment().getType().equals(PunishmentType.TEMP_BAN)) {
                banlist = Bukkit.getBanList(BanList.Type.NAME);
                banlist.addBan(e.getPunishment().getName(), e.getPunishment().getReason(), new Date(e.getPunishment().getEnd()), e.getPunishment().getOperator());
            } else if (e.getPunishment().getType().equals(PunishmentType.IP_BAN) || e.getPunishment().getType().equals(PunishmentType.TEMP_IP_BAN)) {
                banlist = Bukkit.getBanList(BanList.Type.IP);
                banlist.addBan(e.getPunishment().getName(), e.getPunishment().getReason(), new Date(e.getPunishment().getEnd()), e.getPunishment().getOperator());
            }
        } catch (Throwable ignored) {
            // Safely ignored: modern Paper/Mojang authlib lookup fails on Floodgate/Bedrock names starting with '.'
        }
    }
    
    @EventHandler
    public void onRevokePunishment(RevokePunishmentEvent e) {
        try {
            BanList banlist;
            if (e.getPunishment().getType().equals(PunishmentType.BAN) || e.getPunishment().getType().equals(PunishmentType.TEMP_BAN)) {
                banlist = Bukkit.getBanList(BanList.Type.NAME);
                banlist.pardon(e.getPunishment().getName());
            } else if (e.getPunishment().getType().equals(PunishmentType.IP_BAN) || e.getPunishment().getType().equals(PunishmentType.TEMP_IP_BAN)) {
                banlist = Bukkit.getBanList(BanList.Type.IP);
                banlist.pardon(e.getPunishment().getName());
            }
        } catch (Throwable ignored) {
            // Safely ignored: modern Paper/Mojang authlib lookup fails on Floodgate/Bedrock names starting with '.'
        }
    }
}