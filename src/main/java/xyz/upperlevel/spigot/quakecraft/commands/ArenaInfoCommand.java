package xyz.upperlevel.spigot.quakecraft.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;

public class ArenaInfoCommand extends Command {

    public ArenaInfoCommand() {
        super("info");
        setDescription("Shows arena info.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") Arena arena) {
        sender.sendMessage(arena.toInfo());
    }
}
