package xyz.upperlevel.spigot.quakecraft.arena.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;

import static org.bukkit.ChatColor.GREEN;

public class SetKillsToWinCommand extends Command {
    public SetKillsToWinCommand() {
        super("setname");
        setDescription("Sets arena display name.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") Arena arena, @Argument("kills") int killsToWin) {
        arena.setKillsToWin(killsToWin);
        sender.sendMessage(GREEN + "Kills to win set to \"" + killsToWin + "\" for arena \"" + arena.getId() + "\".");
    }
}
