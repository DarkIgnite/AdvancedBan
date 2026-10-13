package me.leoko.advancedban.utils.commands;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.DisguiseManager;
import me.leoko.advancedban.manager.MessageManager;
import me.leoko.advancedban.utils.Command;

import java.util.function.Consumer;

/**
 * Command processor for /bandisguise (/bdisguise).
 */
public class DisguiseProcessor implements Consumer<Command.CommandInput> {

    @Override
    public void accept(Command.CommandInput input) {
        Object sender = input.getSender();
        MethodInterface mi = Universal.get().getMethods();
        String senderName = mi.getName(sender);

        String[] args = input.getArgs();
        String sub = (args != null && args.length > 0 && !args[0].isEmpty()) ? args[0].toLowerCase() : "toggle";

        DisguiseManager manager = DisguiseManager.get();
        String disguisedName = manager.getDisguisedName();

        switch (sub) {
            case "toggle": {
                boolean active = manager.toggle(senderName);
                if (active) {
                    MessageManager.sendMessage(sender, "Disguise.Enabled", true, "NAME", disguisedName);
                } else {
                    MessageManager.sendMessage(sender, "Disguise.Disabled", true, "NAME", senderName);
                }
                break;
            }
            case "on":
            case "enable": {
                manager.setDisguised(senderName, true);
                MessageManager.sendMessage(sender, "Disguise.Enabled", true, "NAME", disguisedName);
                break;
            }
            case "off":
            case "disable": {
                manager.setDisguised(senderName, false);
                MessageManager.sendMessage(sender, "Disguise.Disabled", true, "NAME", senderName);
                break;
            }
            case "status": {
                boolean active = manager.isDisguised(senderName);
                if (active) {
                    MessageManager.sendMessage(sender, "Disguise.StatusOn", true, "NAME", disguisedName);
                } else {
                    MessageManager.sendMessage(sender, "Disguise.StatusOff", true, "NAME", senderName);
                }
                break;
            }
            default:
                MessageManager.sendMessage(sender, "Disguise.Usage", true);
                break;
        }
    }
}
