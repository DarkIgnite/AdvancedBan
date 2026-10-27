package me.leoko.advancedban.utils.commands;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.DatabaseManager;
import me.leoko.advancedban.manager.MessageManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.utils.Command;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;
import me.leoko.advancedban.utils.SQLQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static me.leoko.advancedban.utils.CommandUtils.getPunishment;
import static me.leoko.advancedban.utils.CommandUtils.processName;

public class RevokeProcessor implements Consumer<Command.CommandInput> {
    private PunishmentType type;

    public RevokeProcessor(PunishmentType type) {
        this.type = type;
    }

    @Override
    public void accept(Command.CommandInput input) {
        boolean silent = false;
        String[] args = input.getArgs();
        for (int i = 0; i < args.length && i < 2; i++) {
            if ("-s".equalsIgnoreCase(args[i])) {
                input.removeArgument(i);
                silent = true;
                break;
            }
        }

        String name = input.getPrimary();

        String target = name;
        if(!target.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
            target = processName(input);
            if (target == null)
                return;
        }

        List<Punishment> punishments = new ArrayList<>();
        Punishment p1 = getPunishment(target, type);
        if (p1 != null) {
            punishments.add(p1);
        }
        if (name != null && !name.equalsIgnoreCase(target)) {
            Punishment p2 = getPunishment(name, type);
            if (p2 != null && !punishments.contains(p2)) {
                punishments.add(p2);
            }
        }

        for (Punishment p : PunishmentManager.get().getPunishments(target, type.getBasic(), true)) {
            if (!punishments.contains(p)) {
                punishments.add(p);
            }
        }
        if (name != null && !name.equalsIgnoreCase(target)) {
            for (Punishment p : PunishmentManager.get().getPunishments(name, type.getBasic(), true)) {
                if (!punishments.contains(p)) {
                    punishments.add(p);
                }
            }
        }

        if (punishments.isEmpty()) {
            MessageManager.sendMessage(input.getSender(), "Un" + type.getName() + ".NotPunished",
                    true, "NAME", name);
            return;
        }

        final String operator = Universal.get().getMethods().getName(input.getSender());
        for (Punishment punishment : punishments) {
            punishment.delete(operator, false, true, silent);
        }

        // Direct DB sweep fallback to wipe any duplicate row that might still exist
        DatabaseManager.get().executeStatement(SQLQuery.DELETE_USER_PUNISHMENTS_BY_BASIC_TYPE,
                target, name != null ? name : target,
                type.getBasic() == PunishmentType.BAN ? "BAN" : "MUTE",
                type.getBasic() == PunishmentType.BAN ? "TEMP_BAN" : "TEMP_MUTE");

        // Pardon from vanilla if on Bukkit
        Universal.get().getMethods().pardonVanilla(name, target);

        MessageManager.sendMessage(input.getSender(), "Un" + type.getName() + ".Done",
                true, "NAME", name);
    }
}
