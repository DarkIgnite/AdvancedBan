package me.leoko.advancedban.bukkit;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.bukkit.listener.ChatListener;
import me.leoko.advancedban.bukkit.listener.CommandListener;
import me.leoko.advancedban.bukkit.listener.ConnectionListener;

import org.bukkit.Bukkit;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitMain extends JavaPlugin {
    private static BukkitMain instance;

    public static BukkitMain get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        Universal.get().setup(new BukkitMethods());

        ConnectionListener connListener = new ConnectionListener();
        this.getServer().getPluginManager().registerEvents(connListener, this);
        this.getServer().getPluginManager().registerEvents(new ChatListener(), this);
        this.getServer().getPluginManager().registerEvents(new CommandListener(), this);
        this.getServer().getPluginManager().registerEvents(new me.leoko.advancedban.bukkit.listener.InternalListener(), this);
        me.leoko.advancedban.bukkit.jail.JailManager.init(this);
        this.getServer().getPluginManager().registerEvents(new me.leoko.advancedban.bukkit.jail.JailListener(me.leoko.advancedban.bukkit.jail.JailManager.get()), this);
        cleanVanillaBans();

        Bukkit.getOnlinePlayers().forEach(player -> {
            AsyncPlayerPreLoginEvent apple = new AsyncPlayerPreLoginEvent(player.getName(), player.getAddress().getAddress(), player.getUniqueId());
            connListener.onConnect(apple);
            if (apple.getLoginResult() == AsyncPlayerPreLoginEvent.Result.KICK_BANNED) {
                player.kickPlayer(apple.getKickMessage());
            }
        });

    }

    @Override
    public void onDisable() {
        Universal.get().shutdown();
    }

    /**
     * Pardons existing entries from vanilla ban lists on startup so vanilla never blocks
     * players independently of AdvancedBan database.
     */
    private void cleanVanillaBans() {
        try {
            org.bukkit.BanList nameBans = Bukkit.getBanList(org.bukkit.BanList.Type.NAME);
            for (org.bukkit.BanEntry entry : (java.util.Set<org.bukkit.BanEntry>) nameBans.getEntries()) {
                try {
                    nameBans.pardon(entry.getTarget());
                } catch (Throwable ignored) {}
            }
            org.bukkit.BanList ipBans = Bukkit.getBanList(org.bukkit.BanList.Type.IP);
            for (org.bukkit.BanEntry entry : (java.util.Set<org.bukkit.BanEntry>) ipBans.getEntries()) {
                try {
                    ipBans.pardon(entry.getTarget());
                } catch (Throwable ignored) {}
            }
            getLogger().info("Successfully detached from vanilla ban list and pardoned vanilla entries. Bans are now exclusively managed by AdvancedBan.");
        } catch (Throwable t) {
            getLogger().warning("Could not clear vanilla ban entries: " + t.getMessage());
        }
    }
}
