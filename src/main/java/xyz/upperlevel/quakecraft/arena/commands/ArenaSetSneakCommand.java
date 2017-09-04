package xyz.upperlevel.quakecraft.arena.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import xyz.upperlevel.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.*;

@WithPermission(value = "setsneak", desc = "Allows you to toggle an arena's sneak enabled")
public class ArenaSetSneakCommand extends Command {

    public ArenaSetSneakCommand() {
        super("setsneak");
        setDescription("Sets arena sneak enabled.");
        addAlias("sneak");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") Arena arena, @Optional @Argument("enabled") Boolean enabled) {
        if(enabled == null) {
            enabled = !arena.isSneakEnabled();
        }
        arena.setSneakEnabled(enabled);
        sender.sendMessage(ChatColor.GREEN + arena.getId() + "'s sneak: " + (enabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
    }
}
