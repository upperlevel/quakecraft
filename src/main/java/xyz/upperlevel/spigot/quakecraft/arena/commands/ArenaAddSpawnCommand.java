package xyz.upperlevel.spigot.quakecraft.arena.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.command.Sender;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;


public class ArenaAddSpawnCommand extends Command {
    private static Message SUCCESS;

    public ArenaAddSpawnCommand() {
        super("addspawn");
        setDescription("Adds arena spawn.");
    }

    @Executor(sender = Sender.PLAYER)
    public void run(CommandSender sender, @Argument("arena") Arena arena) {
        arena.addSpawn(((Player) sender).getLocation());
        SUCCESS.send((Player) sender, arena.getPlaceholders());
    }

    public static void loadConfig() {
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("commands.arena.add-spawn");
        SUCCESS = manager.get("success");
    }
}
