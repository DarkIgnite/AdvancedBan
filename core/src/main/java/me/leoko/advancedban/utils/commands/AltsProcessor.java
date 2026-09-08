package me.leoko.advancedban.utils.commands;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.AltManager;
import me.leoko.advancedban.manager.MessageManager;
import me.leoko.advancedban.utils.Command;
import me.leoko.advancedban.utils.DetectedAlt;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static me.leoko.advancedban.utils.CommandUtils.processName;

public class AltsProcessor implements Consumer<Command.CommandInput> {

    @Override
    public void accept(Command.CommandInput input) {
        if (!input.hasNext()) {
            MessageManager.sendMessage(input.getSender(), "Alts.Usage", true);
            return;
        }

        String target = input.getPrimary();
        String name = target;
        String uuid = null;
        String ip = null;

        if (target.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
            ip = target;
            input.next();
        } else {
            uuid = processName(input);
            if (uuid == null) return;
        }

        MethodInterface mi = Universal.get().getMethods();
        List<DetectedAlt> alts = AltManager.get().detectAlts(uuid, name, ip);

        if (alts.isEmpty()) {
            MessageManager.sendMessage(input.getSender(), "Alts.NoEntries", true, "NAME", name, "TARGET", name);
            return;
        }

        int pageSize = mi.getInteger(mi.getConfig(), "Alts.PerPage", 10);
        if (pageSize <= 0) pageSize = 10;

        int page = 1;
        if (input.hasNext()) {
            try {
                page = Integer.parseInt(input.getPrimary());
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        int totalPages = (alts.size() + pageSize - 1) / pageSize;
        if (page < 1 || page > totalPages) {
            MessageManager.sendMessage(input.getSender(), "Alts.OutOfIndex", true, "PAGE", String.valueOf(page));
            return;
        }

        String prefix = MessageManager.getMessage("General.Prefix");
        List<String> header = MessageManager.getLayout(mi.getMessages(), "Alts.Header",
                "PREFIX", prefix,
                "TARGET", name,
                "NAME", name,
                "COUNT", String.valueOf(alts.size()));

        for (String line : header) {
            mi.sendMessage(input.getSender(), line);
        }

        SimpleDateFormat format = new SimpleDateFormat(mi.getString(mi.getConfig(), "DateFormat", "dd.MM.yyyy-HH:mm"));
        boolean canSeeIp = Universal.get().hasPerms(input.getSender(), "ab.alts.ip") || Universal.get().hasPerms(input.getSender(), "ab.check.ip");

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, alts.size());

        for (int i = startIndex; i < endIndex; i++) {
            DetectedAlt alt = alts.get(i);
            String userFormatted = AltManager.formatUserName(alt);
            String kindFormatted = AltManager.formatDetectionKind(alt.getDetectionKind());
            String displayIp = canSeeIp ? alt.getIp() : maskIp(alt.getIp());
            String dateFormatted = format.format(new Date(alt.getLastSeen()));

            String layout = MessageManager.getMessage("Alts.Formatting.Layout",
                    "PREFIX", prefix,
                    "RELEVANT_USER", userFormatted,
                    "RELEVANT_USERID", alt.getUuid(),
                    "ADDRESS", displayIp,
                    "DATE_RECORDED", dateFormatted,
                    "DETECTION_KIND", kindFormatted,
                    "TARGET", name,
                    "USERNAME", alt.getName());

            mi.sendMessage(input.getSender(), layout);
        }

        MessageManager.sendMessage(input.getSender(), "Alts.Footer", false,
                "CURRENT_PAGE", String.valueOf(page),
                "TOTAL_PAGES", String.valueOf(totalPages),
                "COUNT", String.valueOf(alts.size()),
                "TARGET", name,
                "NAME", name);

        if (page < totalPages) {
            MessageManager.sendMessage(input.getSender(), "Alts.PageFooter", false,
                    "NEXT_PAGE", String.valueOf(page + 1),
                    "TARGET", name,
                    "NAME", name);
        }
    }

    private String maskIp(String ip) {
        if (ip == null) return "Unknown";
        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + ".*.*";
        }
        return "*.*.*.*";
    }
}
