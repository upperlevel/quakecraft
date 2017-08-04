package xyz.upperlevel.spigot.quakecraft.arena.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.*;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

@WithPermission(value = "setlobby", desc = "Allows you to set an arena's lobby")
public class ArenaSetLobbyCommand extends Command {
    private static Message SUCCESS;

    public ArenaSetLobbyCommand() {
        super("setlobby");
        setDescription("Sets arena lobby.");
    }

    @Executor(sender = Sender.PLAYER)
    public void run(CommandSender sender, @Argument("arena") Arena arena) {
        arena.setLobby(((Player) sender).getLocation());
        SUCCESS.send((Player) sender, arena.getPlaceholders());
    }

    public static void loadConfig() {
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("commands.arena.set-lobby");
        SUCCESS = manager.get("success");
    }
}
