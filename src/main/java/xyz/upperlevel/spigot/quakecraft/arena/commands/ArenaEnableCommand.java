package xyz.upperlevel.spigot.quakecraft.arena.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;

import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;

public class ArenaEnableCommand extends Command {

    public ArenaEnableCommand() {
        super("enable");
        setDescription("Enables an arena.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") Arena arena) {
        if (!arena.isReady()) {
            sender.sendMessage(RED + "The arena \"" + arena.getId() + "\" is not ready.");
            return;
        }
        if (QuakeCraftReloaded.get().getGameManager().getGame(arena) != null) {
            sender.sendMessage(RED + "The arena \"" + arena.getId() + "\" has already been enabled.");
            return;
        }
        QuakeCraftReloaded.get().getGameManager().addGame(new Game(arena));
        sender.sendMessage(GREEN + "Arena \"" + arena.getId() + "\" enabled successfully.");
    }
}
