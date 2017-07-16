package xyz.upperlevel.spigot.quakecraft.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;

import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;

public class DisableArenaCommand extends Command {

    public DisableArenaCommand() {
        super("disableArena");
        setDescription("Disables an arena.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") Arena arena) {
        if (QuakeCraftReloaded.get().getGameManager().getGame(arena) == null) {
            sender.sendMessage(RED + "The arena \"" + arena.getName() + "\" has already been disabled.");
            return;
        }
        QuakeCraftReloaded.get().getGameManager().removeGame(arena);
        sender.sendMessage(GREEN + "The arena \"" + arena.getName() + "\" disabled successfully.");
    }
}
