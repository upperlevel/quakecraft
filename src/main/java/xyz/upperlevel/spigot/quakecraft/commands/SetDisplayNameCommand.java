package xyz.upperlevel.spigot.quakecraft.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;

import static org.bukkit.ChatColor.*;

public class SetDisplayNameCommand extends Command {

    public SetDisplayNameCommand() {
        super("setdisplayname");
        setDescription("Sets arena display name.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") Arena arena, @Argument("displayname") String[] displayName) {
        String result = String.join(" ", displayName);
        arena.setDisplayName(result);
        sender.sendMessage(GREEN + "Display name set to \"" + result + "\" for arena \"" + arena.getName() + "\".");
    }
}
