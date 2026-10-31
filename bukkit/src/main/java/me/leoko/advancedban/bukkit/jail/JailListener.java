package me.leoko.advancedban.bukkit.jail;

import me.leoko.advancedban.bukkit.event.PunishmentEvent;
import me.leoko.advancedban.bukkit.event.RevokePunishmentEvent;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JailListener implements Listener {

    private final JailManager jailManager;
    private final Map<UUID, Location> pos1Selections = new ConcurrentHashMap<>();
    private final Map<UUID, Location> pos2Selections = new ConcurrentHashMap<>();

    public JailListener(JailManager jailManager) {
        this.jailManager = jailManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWandInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!jailManager.isWand(event.getItem())) return;

        if (!player.hasPermission("ab.jail.setup") && !player.isOp()) {
            player.sendMessage("§cKamu tidak memiliki izin setup jail.");
            event.setCancelled(true);
            return;
        }

        if (event.getClickedBlock() == null) return;
        event.setCancelled(true);

        Location clicked = event.getClickedBlock().getLocation();
        UUID uuid = player.getUniqueId();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            pos1Selections.put(uuid, clicked);
            player.sendMessage("§a[Jail] Posisi 1 ditentukan: §f(" + clicked.getBlockX() + ", " + clicked.getBlockY() + ", " + clicked.getBlockZ() + ")");
            checkAndApplySelection(player);
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            pos2Selections.put(uuid, clicked);
            player.sendMessage("§a[Jail] Posisi 2 ditentukan: §f(" + clicked.getBlockX() + ", " + clicked.getBlockY() + ", " + clicked.getBlockZ() + ")");
            checkAndApplySelection(player);
        }
    }

    private void checkAndApplySelection(Player player) {
        UUID uuid = player.getUniqueId();
        Location p1 = pos1Selections.get(uuid);
        Location p2 = pos2Selections.get(uuid);

        if (p1 != null && p2 != null) {
            if (!p1.getWorld().equals(p2.getWorld())) {
                player.sendMessage("§c[Jail] Posisi 1 dan Posisi 2 harus berada di world yang sama!");
                return;
            }
            jailManager.setRegion(p1, p2);
            player.sendMessage("§6§l[Jail] §aArea penjara berhasil disimpan! Gunakan §e/jail setspawn §auntuk menentukan titik spawn narapidana.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!jailManager.isJailed(player)) return;

        Location to = event.getTo();
        if (to == null) return;

        if (jailManager.isAreaConfigured() && !jailManager.isInJailArea(to)) {
            Location from = event.getFrom();
            if (jailManager.isInJailArea(from)) {
                event.setTo(from);
            } else {
                Location spawn = jailManager.getJailSpawn();
                if (spawn != null) {
                    event.setTo(spawn);
                }
            }
            player.sendMessage("§c§l[JAIL] §cKamu sedang di penjara dan tidak dapat keluar dari area!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (!jailManager.isJailed(player)) return;

        Location dest = event.getTo();
        if (dest != null && jailManager.isAreaConfigured() && !jailManager.isInJailArea(dest)) {
            event.setCancelled(true);
            player.sendMessage("§c§l[JAIL] §cKamu tidak diizinkan melakukan teleportasi keluar penjara!");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!jailManager.isJailed(player)) return;

        String raw = event.getMessage().toLowerCase().trim();
        if (raw.startsWith("/")) {
            raw = raw.substring(1);
        }

        String cmd = raw.split(" ")[0];
        if (cmd.equals("login") || cmd.equals("l") || cmd.equals("register") || cmd.equals("reg")) {
            return; // Allow authentication commands
        }

        event.setCancelled(true);
        player.sendMessage("§c§l[JAIL] §cKamu sedang di penjara! Hanya command login yang diizinkan.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (jailManager.isJailed(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (jailManager.isJailed(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (jailManager.isJailed(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && jailManager.isJailed(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && jailManager.isJailed(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        jailManager.handlePlayerJoin(event.getPlayer());
    }

    @EventHandler
    public void onPunishment(PunishmentEvent event) {
        Punishment punishment = event.getPunishment();
        if (punishment != null && punishment.getType().getBasic() == PunishmentType.JAIL) {
            String uuidStr = punishment.getUuid();
            Player player = null;
            try {
                player = Bukkit.getPlayer(UUID.fromString(uuidStr));
            } catch (Throwable ignored) {
                if (punishment.getName() != null) {
                    player = Bukkit.getPlayer(punishment.getName());
                }
            }
            if (player != null && player.isOnline()) {
                jailManager.jailPlayer(player, punishment);
            }
        }
    }

    @EventHandler
    public void onRevoke(RevokePunishmentEvent event) {
        Punishment punishment = event.getPunishment();
        if (punishment != null && punishment.getType().getBasic() == PunishmentType.JAIL) {
            try {
                UUID uuid = UUID.fromString(punishment.getUuid());
                jailManager.unjailPlayer(uuid);
            } catch (Throwable ignored) {
                if (punishment.getName() != null) {
                    Player p = Bukkit.getPlayer(punishment.getName());
                    if (p != null) {
                        jailManager.unjailPlayer(p.getUniqueId());
                    }
                }
            }
        }
    }
}
