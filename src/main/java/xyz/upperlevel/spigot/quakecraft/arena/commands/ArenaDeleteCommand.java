package xyz.upperlevel.spigot.quakecraft.arena.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;

public class ArenaDeleteCommand extends Command {

    public ArenaDeleteCommand() {
        super("delete");
        setDescription("Deletes an arena.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") String arenaId) {
        if (QuakeCraftReloaded.get().getArenaManager().removeArena(arenaId) == null) {
            sender.sendMessage(ChatColor.RED + "The arena \"" + arenaId + "\" does not exist.");
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "The arena \"" + arenaId + "\" has been deleted successfully.");
    }
}
