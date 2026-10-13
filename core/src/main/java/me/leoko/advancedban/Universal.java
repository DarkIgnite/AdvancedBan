package me.leoko.advancedban;

import com.google.gson.Gson;
import me.leoko.advancedban.manager.*;
import me.leoko.advancedban.utils.Command;
import me.leoko.advancedban.utils.InterimData;
import me.leoko.advancedban.utils.Punishment;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


/**
 * This is the server independent entry point of the plugin.
 */
public class Universal {

    private static Universal instance = null;

    public static void setRedis(boolean redis) {
        Universal.redis = redis;
    }

    private final Map<String, String> ips = new HashMap<>();
    private MethodInterface mi;
    private LogManager logManager;

    private static boolean redis = false;


    private final Gson gson = new Gson();





    /**
     * Get universal.
     *
     * @return the universal instance
     */
    public static Universal get() {
        return instance == null ? instance = new Universal() : instance;
    }

    /**
     * Initially sets up the plugin.
     *
     * @param mi the mi
     */
    public void setup(MethodInterface mi) {
        this.mi = mi;
        checkLegacyMigration();
        mi.loadFiles();
        logManager = new LogManager();
        UpdateManager.get().setup();
        UUIDManager.get().setup();

        try {
            DatabaseManager.get().setup(mi.getBoolean(mi.getConfig(), "UseMySQL", false));
            AltManager.get().setup();
            DisguiseManager.get().setup();
        } catch (Exception ex) {
            log("Failed enabling database-manager...");
            debugException(ex);
        }

        mi.setupMetrics();
        PunishmentManager.get().setup();

        for (Command command : Command.values()) {
            for (String commandName : command.getNames()) {
                mi.setCommandExecutor(commandName, command.getPermission(), command.getTabCompleter());
            }
        }

        String upt = "You have the newest version";
        String response = getFromURL("https://api.spigotmc.org/legacy/update.php?resource=8695");
        if (response == null) {
            upt = "Failed to check for updates :(";
        } else if ((!mi.getVersion().startsWith(response))) {
            upt = "There is a new version available! [" + response + "]";
        }

        if (mi.getBoolean(mi.getConfig(), "DetailedEnableMessage", true)) {
            mi.log("\n \n&8[]=====[&7Enabling AdvancedBanReworked&8]=====[]&r"
                    + "\n&8| &cInformation:&r"
                    + "\n&8|   &cName: &7AdvancedBanReworked&r"
                    + "\n&8|   &cDeveloper: &7Leoko & DarkIgnite&r"
                    + "\n&8|   &cVersion: &7" + mi.getVersion() + "&r"
                    + "\n&8|   &cStorage: &7" + (DatabaseManager.get().isUseMySQL() ? "MySQL (external)" : "HSQLDB (local)") + "&r"
                    + "\n&8| &cSupport:&r"
                    + "\n&8|   &cGithub: &7https://github.com/DarkIgnite/AdvancedBan &r"
                    + "\n&8| &cUpdate:&r"
                    + "\n&8|   &7" + upt  + "&r"
                    + "\n&8[]================================[]&r\n ");
        } else {
            mi.log("&cEnabling AdvancedBanReworked on Version &7&r" + mi.getVersion());
            mi.log("&cCoded by &7Leoko & DarkIgnite&r");
        }
    }

    /**
     * Checks if a legacy AdvancedBan installation exists and automatically
     * migrates the database files and configurations if this is a fresh setup.
     */
    private void checkLegacyMigration() {
        try {
            File dataFolder = mi.getDataFolder();
            if (dataFolder == null) return;
            File pluginsFolder = dataFolder.getParentFile();
            if (pluginsFolder == null) return;

            File legacyFolder = new File(pluginsFolder, "AdvancedBan");
            if (legacyFolder.exists() && legacyFolder.isDirectory() && !dataFolder.getCanonicalPath().equalsIgnoreCase(legacyFolder.getCanonicalPath())) {
                File targetDataDir = new File(dataFolder, "data");
                File legacyDataDir = new File(legacyFolder, "data");
                File targetStorage = new File(targetDataDir, "storage.script");
                File legacyStorage = new File(legacyDataDir, "storage.script");

                if (!targetStorage.exists() && legacyStorage.exists()) {
                    mi.log("§8[§cAdvancedBanReworked§8] §e[Migration] Detected legacy AdvancedBan database! Migrating data to AdvancedBanReworked...");
                    if (!targetDataDir.exists()) {
                        targetDataDir.mkdirs();
                    }
                    File[] files = legacyDataDir.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.isFile() && !f.getName().endsWith(".tmp")) {
                                File targetFile = new File(targetDataDir, f.getName());
                                Files.copy(f.toPath(), targetFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    }
                    mi.log("§8[§cAdvancedBanReworked§8] §a[Migration] Successfully imported legacy punishments and history database!");
                }

                // Also copy configs if they don't exist yet
                String[] configs = new String[]{"config.yml", "Messages.yml", "Layouts.yml"};
                for (String cfg : configs) {
                    File targetCfg = new File(dataFolder, cfg);
                    File legacyCfg = new File(legacyFolder, cfg);
                    if (!targetCfg.exists() && legacyCfg.exists()) {
                        if (!dataFolder.exists()) {
                            dataFolder.mkdirs();
                        }
                        Files.copy(legacyCfg.toPath(), targetCfg.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        mi.log("§8[§cAdvancedBanReworked§8] §a[Migration] Imported legacy " + cfg + "!");
                    }
                }
            }
        } catch (Exception ex) {
            mi.log("§8[§cAdvancedBanReworked§8] §c[Migration] Warning: Could not complete legacy migration: " + ex.getMessage());
        }
    }

    /**
     * Shutdown.
     */
    public void shutdown() {
        DisguiseManager.get().save();
        DatabaseManager.get().shutdown();

        if (mi.getBoolean(mi.getConfig(), "DetailedDisableMessage", true)) {
            mi.log("\n \n&8[]=====[&7Disabling AdvancedBanReworked&8]=====[]"
                    + "\n&8| &cInformation:"
                    + "\n&8|   &cName: &7AdvancedBanReworked"
                    + "\n&8|   &cDeveloper: &7Leoko & DarkIgnite"
                    + "\n&8|   &cVersion: &7" + getMethods().getVersion()
                    + "\n&8|   &cStorage: &7" + (DatabaseManager.get().isUseMySQL() ? "MySQL (external)" : "HSQLDB (local)")
                    + "\n&8| &cSupport:"
                    + "\n&8|   &cGithub: &7https://github.com/DarkIgnite/AdvancedBan"
                    + "\n&8[]================================[]&r\n ");
        } else {
            mi.log("&cDisabling AdvancedBanReworked on Version &7" + getMethods().getVersion());
            mi.log("&cCoded by Leoko & DarkIgnite");
        }
    }

    /**
     * Gets methods.
     *
     * @return the methods
     */
    public MethodInterface getMethods() {
        return mi;
    }

    /**
     * Is bungee boolean.
     *
     * @return the boolean
     */
    public boolean isBungee() {
        return mi.isBungee();
    }

    public Map<String, String> getIps() {
        return ips;
    }

    public static boolean isRedis() {
        return redis;
    }

    public Gson getGson() {
        return gson;
    }

    /**
     * Gets from url.
     *
     * @param surl the surl
     * @return the from url
     */
    public String getFromURL(String surl) {
        String response = null;
        try {
            URL url = new URL(surl);
            Scanner s = new Scanner(url.openStream());
            if (s.hasNext()) {
                response = s.next();
                s.close();
            }
        } catch (IOException exc) {
            debug("!! Failed to connect to URL: " + surl);
        }
        return response;
    }

    /**
     * Is mute command boolean.
     *
     * @param cmd the cmd
     * @return the boolean
     */
    public boolean isMuteCommand(String cmd) {
        return isMuteCommand(cmd, getMethods().getStringList(getMethods().getConfig(), "MuteCommands"));
    }

    /**
     * Visible for testing. Do not use this. Please use {@link #isMuteCommand(String)}.
     * 
     * @param cmd          the command
     * @param muteCommands the mute commands from the config
     * @return true if the command matched any of the mute commands.
     */
    boolean isMuteCommand(String cmd, List<String> muteCommands) {
        String[] words = cmd.split(" ");
        // Handle commands with colons
        if (words[0].indexOf(':') != -1) {
            words[0] = words[0].split(":", 2)[1];
        }
        for (String muteCommand : muteCommands) {
            if (muteCommandMatches(words, muteCommand)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Visible for testing. Do not use this.
     * 
     * @param commandWords the command run by a player, separated into its words
     * @param muteCommand a mute command from the config
     * @return true if they match, false otherwise
     */
    boolean muteCommandMatches(String[] commandWords, String muteCommand) {
        // Basic equality check
        if (commandWords[0].equalsIgnoreCase(muteCommand)) {
            return true;
        }
        // Advanced equality check
        // Essentially a case-insensitive "startsWith" for arrays
        if (muteCommand.indexOf(' ') != -1) {
            String[] muteCommandWords = muteCommand.split(" ");
            if (muteCommandWords.length > commandWords.length) {
                return false;
            }
            for (int n = 0; n < muteCommandWords.length; n++) {
                if (!muteCommandWords[n].equalsIgnoreCase(commandWords[n])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Is exempt player boolean.
     *
     * @param name the name
     * @return the boolean
     */
    public boolean isExemptPlayer(String name) {
        List<String> exempt = getMethods().getStringList(getMethods().getConfig(), "ExemptPlayers");
        if (exempt != null) {
            for (String str : exempt) {
                if (name.equalsIgnoreCase(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Broadcast leoko boolean.
     *
     * @return the boolean
     */
    public boolean broadcastLeoko() {
        File readme = new File(getMethods().getDataFolder(), "readme.txt");
        if (!readme.exists()) {
            return true;
        }
        try {
            if (Files.readAllLines(Paths.get(readme.getPath()), Charset.defaultCharset()).get(0).equalsIgnoreCase("I don't want that there will be any message when the dev of this plugin joins the server! I want this even though the plugin is 100% free and the join-message is the only reward for the Dev :(")) {
                return false;
            }
        } catch (IOException ignore) {
        }
        return true;
    }

    /**
     * Call connection string.
     *
     * @param name the name
     * @param ip   the ip
     * @return the string
     */
    public String callConnection(String name, String ip) {
        name = name.toLowerCase();
        String uuid = UUIDManager.get().getUUID(name);
        if (uuid == null) return "[AdvancedBan] Failed to fetch your UUID";

        if (ip != null) {
            getIps().remove(name);
            getIps().put(name, ip);
        }

        InterimData interimData = PunishmentManager.get().load(name, uuid, ip);

        if (interimData == null) {
            if (getMethods().getBoolean(mi.getConfig(), "LockdownOnError", true)) {
                return "[AdvancedBan] Failed to load player data!";
            } else {
                return null;
            }
        }

        Punishment pt = interimData.getBan();

        if (pt == null) {
            interimData.accept();
            AltManager.get().recordConnection(uuid, name, ip);
            AltManager.get().checkAndNotifyOnJoin(name, uuid, ip);
            return null;
        }

        return pt.getLayoutBSN();
    }

    /**
     * Has perms boolean.
     *
     * @param player the player
     * @param perms  the perms
     * @return the boolean
     */
    public boolean hasPerms(Object player, String perms) {
        if (mi.hasPerms(player, perms)) {
            return true;
        }

        if (mi.getBoolean(mi.getConfig(), "EnableAllPermissionNodes", false)) {
            while (perms.contains(".")) {
                perms = perms.substring(0, perms.lastIndexOf('.'));
                if (mi.hasPerms(player, perms + ".all")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Log.
     *
     * @param msg the msg
     */
    public void log(String msg) {
        mi.log("§8[§cAdvancedBan§8] §7" + msg);
        debugToFile(msg);
    }

    /**
     * Debug.
     *
     * @param msg the msg
     */
    public void debug(Object msg) {
        if (mi.getBoolean(mi.getConfig(), "Debug", false)) {
            mi.log("§8[§cAdvancedBan§8] §cDebug: §7" + msg.toString());
        }
        debugToFile(msg);
    }

    public void debugException(Exception exc) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exc.printStackTrace(pw);
        debug(sw.toString());
    }

    /**
     * Debug.
     *
     * @param ex the ex
     */
    public void debugSqlException(SQLException ex) {
        if (mi.getBoolean(mi.getConfig(), "Debug", false)) {
            debug("§7An error has occurred with the database, the error code is: '" + ex.getErrorCode() + "'");
            debug("§7The state of the sql is: " + ex.getSQLState());
            debug("§7Error message: " + ex.getMessage());
        }
        debugException(ex);
    }

    private void debugToFile(Object msg) {
        if (logManager == null || mi == null || mi.getDataFolder() == null) {
            return;
        }
        File logsDir = new File(mi.getDataFolder(), "logs");
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }
        File debugFile = new File(logsDir, "latest.log");
        if (!debugFile.exists()) {
            try {
                debugFile.createNewFile();
            } catch (IOException ex) {
                System.out.print("An error has occurred creating the 'latest.log' file again, check your server.");
                System.out.print("Error message" + ex.getMessage());
            }
        } else {
            logManager.checkLastLog(false);
        }
        try {
            FileUtils.writeStringToFile(debugFile, "[" + new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis()) + "] " + mi.clearFormatting(msg.toString()) + "\n", "UTF8", true);
        } catch (IOException ex) {
            System.out.print("An error has occurred writing to 'latest.log' file.");
            System.out.print(ex.getMessage());
        }
    }
}
