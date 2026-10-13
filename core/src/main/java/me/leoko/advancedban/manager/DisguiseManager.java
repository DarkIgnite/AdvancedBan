package me.leoko.advancedban.manager;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages ban disguise mode for staff members.
 * When disguise is enabled for a staff member, public punishment notifications,
 * broadcasts, and kick/ban screens display as CONSOLE (or configured DisguisedName).
 * The real staff member name is ALWAYS preserved in the database and visible in
 * /banlist, /history, and /check.
 */
public class DisguiseManager {
    private static DisguiseManager instance;
    private final Set<String> disguisedStaff = ConcurrentHashMap.newKeySet();
    private File storageFile;

    public static synchronized DisguiseManager get() {
        if (instance == null) {
            instance = new DisguiseManager();
        }
        return instance;
    }

    public void setup() {
        MethodInterface mi = Universal.get().getMethods();
        File dataFolder = mi.getDataFolder();
        if (dataFolder != null) {
            storageFile = new File(dataFolder, "disguised_staff.txt");
            load();
        }
    }

    public void load() {
        if (storageFile != null && storageFile.exists()) {
            try {
                disguisedStaff.clear();
                for (String line : Files.readAllLines(storageFile.toPath(), StandardCharsets.UTF_8)) {
                    String trimmed = line.trim().toLowerCase();
                    if (!trimmed.isEmpty()) {
                        disguisedStaff.add(trimmed);
                    }
                }
            } catch (IOException e) {
                Universal.get().debug("Failed to load disguised staff: " + e.getMessage());
            }
        }
    }

    public void save() {
        if (storageFile != null) {
            try {
                if (storageFile.getParentFile() != null && !storageFile.getParentFile().exists()) {
                    storageFile.getParentFile().mkdirs();
                }
                Files.write(storageFile.toPath(), disguisedStaff, StandardCharsets.UTF_8);
            } catch (IOException e) {
                Universal.get().debug("Failed to save disguised staff: " + e.getMessage());
            }
        }
    }

    public boolean isDisguised(String staffName) {
        if (staffName == null) return false;
        return disguisedStaff.contains(staffName.toLowerCase());
    }

    public boolean toggle(String staffName) {
        if (staffName == null) return false;
        String key = staffName.toLowerCase();
        boolean newState;
        if (disguisedStaff.contains(key)) {
            disguisedStaff.remove(key);
            newState = false;
        } else {
            disguisedStaff.add(key);
            newState = true;
        }
        save();
        return newState;
    }

    public void setDisguised(String staffName, boolean disguised) {
        if (staffName == null) return;
        String key = staffName.toLowerCase();
        if (disguised) {
            disguisedStaff.add(key);
        } else {
            disguisedStaff.remove(key);
        }
        save();
    }

    public String getDisguisedName() {
        MethodInterface mi = Universal.get().getMethods();
        return mi.getString(mi.getConfig(), "Disguise.DisguisedName", "CONSOLE");
    }

    public String getDisplayOperator(String operator) {
        if (operator != null && isDisguised(operator)) {
            return getDisguisedName();
        }
        return operator;
    }

    public Set<String> getDisguisedStaff() {
        return Collections.unmodifiableSet(disguisedStaff);
    }
}
