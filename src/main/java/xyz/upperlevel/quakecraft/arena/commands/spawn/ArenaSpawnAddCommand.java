package xyz.upperlevel.quakecraft.arena.commands.spawn;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.*;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;


@WithPermission(value = "add", desc = "Allows you to add a spawn to an arena")
public class ArenaSpawnAddCommand extends Command {
    private static Message SUCCESS;

    public ArenaSpawnAddCommand() {
        super("add");
        setDescription("Adds arena spawn.");
    }

    @Executor(sender = Sender.PLAYER)
    public void run(CommandSender sender, @Argument("arena") Arena arena) {
        arena.addSpawn(((Player) sender).getLocation());
        SUCCESS.send((Player) sender, arena.getPlaceholders());
    }

    public static void loadConfig() {
        MessageManager manager = Quakecraft.get().getMessages().getSection("commands.arena.spawn.add");
        SUCCESS = manager.get("success");
    }
}
