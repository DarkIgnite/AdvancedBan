package me.leoko.advancedban.bukkit.jail;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.bukkit.BukkitMain;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class JailManager {

    private static JailManager instance;
    private final BukkitMain plugin;

    private final NamespacedKey wandKey;
    private File jailConfigFile;
    private YamlConfiguration jailConfig;
    private File jailedPlayersFolder;

    private String jailWorldName;
    private Integer minX, maxX, minY, maxY, minZ, maxZ;
    private Location jailSpawn;

    public JailManager(BukkitMain plugin) {
        this.plugin = plugin;
        this.wandKey = new NamespacedKey(plugin, "jail_wand");
        loadJailConfig();
        startExpirationTask();
    }

    public static synchronized void init(BukkitMain plugin) {
        if (instance == null) {
            instance = new JailManager(plugin);
        }
    }

    public static JailManager get() {
        return instance;
    }

    public void loadJailConfig() {
        jailConfigFile = new File(plugin.getDataFolder(), "jail.yml");
        if (!jailConfigFile.exists()) {
            plugin.saveResource("jail.yml", false);
        }
        jailConfig = YamlConfiguration.loadConfiguration(jailConfigFile);

        jailedPlayersFolder = new File(plugin.getDataFolder(), "jailed_players");
        if (!jailedPlayersFolder.exists()) {
            jailedPlayersFolder.mkdirs();
        }

        jailWorldName = jailConfig.getString("area.world");
        if (jailConfig.contains("area.minX")) {
            minX = jailConfig.getInt("area.minX");
            maxX = jailConfig.getInt("area.maxX");
            minY = jailConfig.getInt("area.minY");
            maxY = jailConfig.getInt("area.maxY");
            minZ = jailConfig.getInt("area.minZ");
            maxZ = jailConfig.getInt("area.maxZ");
        } else {
            minX = maxX = minY = maxY = minZ = maxZ = null;
        }

        if (jailConfig.contains("spawn.world")) {
            World w = Bukkit.getWorld(jailConfig.getString("spawn.world"));
            if (w != null) {
                jailSpawn = new Location(
                        w,
                        jailConfig.getDouble("spawn.x"),
                        jailConfig.getDouble("spawn.y"),
                        jailConfig.getDouble("spawn.z"),
                        (float) jailConfig.getDouble("spawn.yaw"),
                        (float) jailConfig.getDouble("spawn.pitch")
                );
            }
        }
    }

    public void saveJailConfig() {
        try {
            if (jailWorldName != null) jailConfig.set("area.world", jailWorldName);
            if (minX != null) {
                jailConfig.set("area.minX", minX);
                jailConfig.set("area.maxX", maxX);
                jailConfig.set("area.minY", minY);
                jailConfig.set("area.maxY", maxY);
                jailConfig.set("area.minZ", minZ);
                jailConfig.set("area.maxZ", maxZ);
            }
            if (jailSpawn != null) {
                jailConfig.set("spawn.world", jailSpawn.getWorld().getName());
                jailConfig.set("spawn.x", jailSpawn.getX());
                jailConfig.set("spawn.y", jailSpawn.getY());
                jailConfig.set("spawn.z", jailSpawn.getZ());
                jailConfig.set("spawn.yaw", jailSpawn.getYaw());
                jailConfig.set("spawn.pitch", jailSpawn.getPitch());
            }
            jailConfig.save(jailConfigFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save jail.yml: " + e.getMessage());
        }
    }

    public boolean isAreaConfigured() {
        return jailWorldName != null && minX != null && jailSpawn != null;
    }

    public boolean isInJailArea(Location loc) {
        if (!isAreaConfigured() || loc == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().getName().equalsIgnoreCase(jailWorldName)) return false;
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public Location getJailSpawn() {
        return jailSpawn;
    }

    public void setSpawn(Location loc) {
        this.jailSpawn = loc.clone();
        saveJailConfig();
    }

    public void setRegion(Location pos1, Location pos2) {
        this.jailWorldName = pos1.getWorld().getName();
        this.minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        this.maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        this.minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        this.maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        this.minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        this.maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        saveJailConfig();
    }

    public ItemStack createWand() {
        ItemStack wand = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§lJail Setup Wand");
            meta.setLore(Arrays.asList(
                    "§7Alat untuk menentukan area penjara.",
                    "§a• Klik Kiri Blok: §fSet Posisi 1",
                    "§a• Klik Kanan Blok: §fSet Posisi 2",
                    "§eGunakan §f/jail setspawn §euntuk menentukan titik spawn."
            ));
            meta.getPersistentDataContainer().set(wandKey, PersistentDataType.BYTE, (byte) 1);
            wand.setItemMeta(meta);
        }
        return wand;
    }

    public boolean isWand(ItemStack item) {
        if (item == null || item.getType() != Material.GOLDEN_AXE) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(wandKey, PersistentDataType.BYTE);
    }

    public boolean isJailed(Player player) {
        if (player == null) return false;
        return PunishmentManager.get().isJailed(player.getUniqueId().toString())
                || PunishmentManager.get().isJailed(player.getName());
    }

    public void jailPlayer(Player player, Punishment punishment) {
        if (player == null || !player.isOnline()) return;

        File playerFile = new File(jailedPlayersFolder, player.getUniqueId() + ".yml");
        if (!playerFile.exists()) {
            YamlConfiguration data = new YamlConfiguration();
            Location loc = player.getLocation();
            data.set("original_location.world", loc.getWorld().getName());
            data.set("original_location.x", loc.getX());
            data.set("original_location.y", loc.getY());
            data.set("original_location.z", loc.getZ());
            data.set("original_location.yaw", loc.getYaw());
            data.set("original_location.pitch", loc.getPitch());

            String primaryGroup = getLuckPermsPrimaryGroup(player);
            data.set("original_parent", primaryGroup);

            data.set("inventory", player.getInventory().getContents());
            data.set("armor", player.getInventory().getArmorContents());
            data.set("extra", player.getInventory().getExtraContents());

            try {
                data.save(playerFile);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save jailed player data for " + player.getName() + ": " + e.getMessage());
            }
        }

        // Clear inventory
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setItemInOffHand(null);

        // LuckPerms parent set tahanan
        setLuckPermsParent(player.getName(), "tahanan");

        // Teleport to jail spawn
        if (jailSpawn != null) {
            player.teleport(jailSpawn);
        }

        player.sendTitle("§c§lPENJARA", "§7Kamu dimasukkan ke dalam penjara!", 10, 70, 20);
        player.sendMessage("§c§l[JAIL] §cKamu telah dijebloskan ke dalam penjara!");
        player.sendMessage("§c§l[JAIL] §7Inventory kamu disimpan dan kamu tidak dapat keluar area.");
        player.sendMessage("§c§l[JAIL] §7Selama di dalam penjara hanya command login yang diizinkan.");
    }

    public void unjailPlayer(UUID uuid) {
        File playerFile = new File(jailedPlayersFolder, uuid + ".yml");
        String originalParent = "default";
        Location originalLoc = null;
        ItemStack[] inventory = null;
        ItemStack[] armor = null;
        ItemStack[] extra = null;

        if (playerFile.exists()) {
            YamlConfiguration data = YamlConfiguration.loadConfiguration(playerFile);
            originalParent = data.getString("original_parent", "default");

            if (data.contains("original_location.world")) {
                World w = Bukkit.getWorld(data.getString("original_location.world"));
                if (w != null) {
                    originalLoc = new Location(
                            w,
                            data.getDouble("original_location.x"),
                            data.getDouble("original_location.y"),
                            data.getDouble("original_location.z"),
                            (float) data.getDouble("original_location.yaw"),
                            (float) data.getDouble("original_location.pitch")
                    );
                }
            }

            try {
                List<?> invList = data.getList("inventory");
                if (invList != null) inventory = invList.toArray(new ItemStack[0]);
                List<?> armList = data.getList("armor");
                if (armList != null) armor = armList.toArray(new ItemStack[0]);
                List<?> extList = data.getList("extra");
                if (extList != null) extra = extList.toArray(new ItemStack[0]);
            } catch (Throwable ignored) {}
        }

        Player player = Bukkit.getPlayer(uuid);
        String name = (player != null) ? player.getName() : uuid.toString();

        // Restore LuckPerms parent
        setLuckPermsParent(name, originalParent);

        if (player != null && player.isOnline()) {
            player.getInventory().clear();
            if (inventory != null) player.getInventory().setContents(inventory);
            if (armor != null) player.getInventory().setArmorContents(armor);
            if (extra != null) player.getInventory().setExtraContents(extra);

            if (originalLoc != null && !isInJailArea(originalLoc)) {
                player.teleport(originalLoc);
            } else {
                player.teleport(player.getWorld().getSpawnLocation());
            }

            player.sendTitle("§a§lBEBAS!", "§7Masa tahanan penjara kamu telah selesai.", 10, 70, 20);
            player.sendMessage("§a§l[JAIL] §aMasa tahanan kamu selesai. Seluruh barang bawaanmu telah dikembalikan!");

            if (playerFile.exists()) {
                playerFile.delete();
            }
        }
    }

    public void handlePlayerJoin(Player player) {
        UUID uuid = player.getUniqueId();
        File playerFile = new File(jailedPlayersFolder, uuid + ".yml");

        boolean jailed = isJailed(player);

        if (jailed) {
            // Keep player jailed
            Punishment p = PunishmentManager.get().getJail(uuid.toString());
            if (p == null) p = PunishmentManager.get().getJail(player.getName());
            if (p != null) {
                jailPlayer(player, p);
            }
        } else if (playerFile.exists()) {
            // Player was unjailed while offline!
            unjailPlayer(uuid);
        }
    }

    private String getLuckPermsPrimaryGroup(Player player) {
        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            try {
                Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
                Object lp = providerClass.getMethod("get").invoke(null);
                Object userManager = lp.getClass().getMethod("getUserManager").invoke(lp);
                Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, player.getUniqueId());
                if (user != null) {
                    return (String) user.getClass().getMethod("getPrimaryGroup").invoke(user);
                }
            } catch (Throwable ignored) {}
        }
        return "default";
    }

    private void setLuckPermsParent(String playerName, String parentGroup) {
        if (parentGroup == null || parentGroup.isEmpty()) parentGroup = "default";
        final String group = parentGroup;
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " parent set " + group);
        });
    }

    private void startExpirationTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                File file = new File(jailedPlayersFolder, player.getUniqueId() + ".yml");
                if (file.exists()) {
                    Punishment p = PunishmentManager.get().getJail(player.getUniqueId().toString());
                    if (p == null) p = PunishmentManager.get().getJail(player.getName());
                    if (p == null || p.isExpired()) {
                        unjailPlayer(player.getUniqueId());
                    }
                }
            }
        }, 20L, 20L);
    }

    public void handleAdminCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ab.jail.setup") && !sender.isOp()) {
            sender.sendMessage("§cKamu tidak memiliki izin untuk setup jail.");
            return;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "wand" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cCommand ini hanya untuk player in-game!");
                    return;
                }
                player.getInventory().addItem(createWand());
                player.sendMessage("§a[Jail] Kamu menerima Jail Setup Wand! Klik kiri blok untuk Pos 1, klik kanan untuk Pos 2.");
            }
            case "setspawn" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cCommand ini hanya untuk player in-game!");
                    return;
                }
                setSpawn(player.getLocation());
                player.sendMessage("§a[Jail] Titik spawn penjara berhasil di-set ke posisimu saat ini!");
            }
            case "setup", "status" -> {
                sender.sendMessage("§6===== [ AdvancedBan Jail Setup ] =====");
                sender.sendMessage("§7Area World: §f" + (jailWorldName != null ? jailWorldName : "§cBelum di-set"));
                if (minX != null) {
                    sender.sendMessage("§7Area Min: §f(" + minX + ", " + minY + ", " + minZ + ")");
                    sender.sendMessage("§7Area Max: §f(" + maxX + ", " + maxY + ", " + maxZ + ")");
                } else {
                    sender.sendMessage("§7Area Bounds: §cBelum di-set (Gunakan /jail wand)");
                }
                if (jailSpawn != null) {
                    sender.sendMessage("§7Spawn: §f(" + jailSpawn.getBlockX() + ", " + jailSpawn.getBlockY() + ", " + jailSpawn.getBlockZ() + " di " + jailSpawn.getWorld().getName() + ")");
                } else {
                    sender.sendMessage("§7Spawn: §cBelum di-set (Gunakan /jail setspawn)");
                }
                sender.sendMessage("§ePerintah setup:");
                sender.sendMessage("§f/jail wand §7- Ambil wand pemilihan area");
                sender.sendMessage("§f/jail setspawn §7- Set titik spawn penjara");
            }
            default -> {
                sender.sendMessage("§cSubcommand setup tidak dikenal. Ketik §e/jail setup §cuntuk melihat panduan.");
            }
        }
    }
}
