package xyz.upperlevel.spigot.quakecraft.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;

public class DeleteArenaCommand extends Command {

    public DeleteArenaCommand() {
        super("delete");
        setDescription("Deletes an arena.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") String arenaName) {
        if (QuakeCraftReloaded.get().getArenaManager().removeArena(arenaName) == null) {
            sender.sendMessage(ChatColor.RED + "The arena \"" + arenaName + "\" does not exist.");
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "The arena \"" + arenaName + "\" has been deleted successfully.");
    }
}
