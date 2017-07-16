package xyz.upperlevel.spigot.quakecraft.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;

import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;

public class EnableArenaCommand extends Command {

    public EnableArenaCommand() {
        super("enableArena");
        setDescription("Enables an arena.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") Arena arena) {
        if (!arena.isReady()) {
            sender.sendMessage(RED + "The arena \"" + arena.getName() + "\" is not ready.");
            return;
        }
        if (QuakeCraftReloaded.get().getGameManager().getGame(arena) != null) {
            sender.sendMessage(RED + "The arena \"" + arena.getName() + "\" has already been enabled.");
            return;
        }
        QuakeCraftReloaded.get().getGameManager().addGame(new Game(arena));
        sender.sendMessage(GREEN + "Arena \"" + arena.getName() + "\" enabled successfully.");
    }
}
