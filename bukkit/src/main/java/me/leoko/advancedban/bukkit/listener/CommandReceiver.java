package me.leoko.advancedban.bukkit.listener;

import me.leoko.advancedban.bukkit.jail.JailManager;
import me.leoko.advancedban.manager.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.regex.Pattern;

public class CommandReceiver implements CommandExecutor {
    private static CommandReceiver instance = null;
    private static final Pattern DURATION_PATTERN = Pattern.compile("^[1-9][0-9]*([wdhms]|mo)$", Pattern.CASE_INSENSITIVE);

    public static CommandReceiver get() {
        return instance == null ? instance = new CommandReceiver() : instance;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (command.getName().equalsIgnoreCase("jail") && strings.length > 0) {
            String first = strings[0].toLowerCase();
            if (first.equals("wand") || first.equals("setspawn") || first.equals("setup") || first.equals("status")) {
                JailManager.get().handleAdminCommand(commandSender, strings);
                return true;
            }
        }

        if (strings.length > 0) {
            strings[0] = (Bukkit.getPlayer(strings[0]) != null ? Bukkit.getPlayer(strings[0]).getName() : strings[0]);
        }

        // Support /jail <player> <duration> <reason> as tempjail automatically
        if (command.getName().equalsIgnoreCase("jail") && strings.length >= 2) {
            int durationIndex = strings[0].equalsIgnoreCase("-s") ? 2 : 1;
            if (strings.length > durationIndex) {
                String potentialDuration = strings[durationIndex];
                if (DURATION_PATTERN.matcher(potentialDuration).matches() || potentialDuration.startsWith("#")) {
                    CommandManager.get().onCommand(commandSender, "tempjail", strings);
                    return true;
                }
            }
        }

        CommandManager.get().onCommand(commandSender, command.getName(), strings);
        return true;
    }
}
