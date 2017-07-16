package xyz.upperlevel.spigot.quakecraft.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.command.Sender;

public class SetLobbyCommand extends Command {

    public SetLobbyCommand() {
        super("setlobby");
        setDescription("Sets arena lobby.");
    }

    @Executor(sender = Sender.PLAYER)
    public void run(CommandSender sender, @Argument("arena") Arena arena) {
        arena.setLobby(((Player) sender).getLocation());
        sender.sendMessage(ChatColor.GREEN + "Lobby sets successfully!");
    }
}
