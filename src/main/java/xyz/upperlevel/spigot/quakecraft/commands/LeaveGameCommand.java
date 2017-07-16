package xyz.upperlevel.spigot.quakecraft.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;

import java.util.Arrays;
import java.util.List;

public class LeaveGameCommand extends Command {

    public LeaveGameCommand() {
        super("leave");
        setDescription("Leaves from the arena joined.");
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("quit", "exit");
    }

    @Executor
    public void run(CommandSender sender) {
        Game game = QuakeCraftReloaded.get().getGameManager().getGame((Player) sender);
        if (game == null) {
            sender.sendMessage(ChatColor.RED + "You are not in any arena.");
            return;
        }
        game.leave((Player) sender);
        sender.sendMessage(ChatColor.GREEN + "You left the arena \"" + game.getArena().getName() + "\".");
    }
}
