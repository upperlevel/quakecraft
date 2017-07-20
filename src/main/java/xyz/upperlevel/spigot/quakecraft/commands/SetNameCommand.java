package xyz.upperlevel.spigot.quakecraft.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;

import static org.bukkit.ChatColor.*;

public class SetNameCommand extends Command {

    public SetNameCommand() {
        super("setname");
        setDescription("Sets arena display name.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") Arena arena, @Argument("name") String[] name) {
        String result = String.join(" ", name);
        arena.setName(result);
        sender.sendMessage(GREEN + "Name set to \"" + result + "\" for arena \"" + arena.getName() + "\".");
    }
}
