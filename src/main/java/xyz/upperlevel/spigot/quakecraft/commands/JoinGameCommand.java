package xyz.upperlevel.spigot.quakecraft.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.command.Sender;

public class JoinGameCommand extends Command {

    public JoinGameCommand() {
        super("join");
        setDescription("Joins an arena.");
    }

    @Executor(sender = Sender.PLAYER)
    public void run(CommandSender sender, @Argument("arena") Arena arena) {
        Game game = QuakeCraftReloaded.get().getGameManager().getGame(arena);
        if (game == null) {
            sender.sendMessage(ChatColor.RED + "Any game found by: " + arena.getName()); // todo config
            return;
        }
        game.join((Player) sender);
        sender.sendMessage(ChatColor.GREEN + "The game '" + game.getArena().getName() + "' has been joined successfully!");
    }

}
