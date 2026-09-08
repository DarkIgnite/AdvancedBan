package me.leoko.advancedban.utils.commands;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.AltManager;
import me.leoko.advancedban.manager.GeoIPManager;
import me.leoko.advancedban.manager.MessageManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.utils.Command;
import me.leoko.advancedban.utils.GeoIPResult;

import java.util.function.Consumer;

/**
 * Command processor for /guard (analyze <player>, help).
 */
public class GuardProcessor implements Consumer<Command.CommandInput> {

    @Override
    public void accept(Command.CommandInput input) {
        Object sender = input.getSender();
        String[] args = input.getArgs();

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("analyze")) {
            if (args.length < 2) {
                MessageManager.sendMessage(sender, "Guard.Usage", true);
                return;
            }

            String target = args[1];
            String ip = null;
            String name = target;
            String uuid = null;
            String status;

            if (target.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
                ip = target;
                status = "IP";
            } else {
                boolean online = Universal.get().getMethods().isOnline(target);
                status = online ? MessageManager.getMessage("Guard.Online", false)
                        : MessageManager.getMessage("Guard.Offline", false);

                uuid = UUIDManager.get().getUUID(target);
                ip = Universal.get().getIps().get(target.toLowerCase());

                if ((ip == null || ip.equalsIgnoreCase("none cached") || ip.trim().isEmpty()) && uuid != null) {
                    ip = AltManager.get().getLatestAddress(uuid);
                }
            }

            if (ip == null || ip.equalsIgnoreCase("none cached") || ip.trim().isEmpty()) {
                MessageManager.sendMessage(sender, "Guard.NoData", true, "NAME", target);
                return;
            }

            String cleanIp = ip.trim().replace("/", "");
            boolean canViewIp = Universal.get().hasPerms(sender, "ab.guard.ip");
            String displayIp = canViewIp ? cleanIp : maskIp(cleanIp);

            GeoIPResult geo = GeoIPManager.get().analyze(cleanIp);
            int altsCount = uuid != null ? AltManager.get().getAltsCount(uuid, name, cleanIp) : 0;

            MessageManager.sendMessage(sender, "Guard.Header", true, "NAME", name);
            MessageManager.sendMessage(sender, "Guard.Player", false, "NAME", name, "STATUS", status);
            MessageManager.sendMessage(sender, "Guard.IP", false, "IP", displayIp);
            MessageManager.sendMessage(sender, "Guard.Country", false, "COUNTRY", geo.getCountry(), "CODE", geo.getCountryCode());
            MessageManager.sendMessage(sender, "Guard.City", false, "CITY", geo.getCity(), "ACCURACY", String.valueOf(geo.getAccuracy()));
            MessageManager.sendMessage(sender, "Guard.Region", false, "REGION", geo.getRegion());
            MessageManager.sendMessage(sender, "Guard.ISP", false, "ISP", geo.getIsp());
            MessageManager.sendMessage(sender, "Guard.Timezone", false, "TIMEZONE", geo.getTimezone());
            MessageManager.sendMessage(sender, "Guard.Alts", false, "COUNT", String.valueOf(altsCount));
            MessageManager.sendMessage(sender, "Guard.Footer", false);
            return;
        }

        MessageManager.sendMessage(sender, "Guard.Usage", true);
    }

    private void sendHelp(Object sender) {
        MessageManager.sendMessage(sender, "Guard.Help.Header", true);
        MessageManager.sendMessage(sender, "Guard.Help.Analyze", false);
        MessageManager.sendMessage(sender, "Guard.Help.Footer", false);
    }

    private String maskIp(String ip) {
        if (ip == null) {
            return "Unknown";
        }
        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + ".***.***";
        }
        return ip.replaceAll("[0-9a-fA-F]{3,}$", "***");
    }
}
