package xyz.upperlevel.spigot.quakecraft.arena.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.command.WithPermission;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

@WithPermission(value = "info", desc = "Allows you to get infos from command")
public class ArenaInfoCommand extends Command {
    private static Message FORMAT;

    public ArenaInfoCommand() {
        super("info");
        setDescription("Shows arena info.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") Arena arena) {
        FORMAT.send(sender, arena.getPlaceholders());
    }

    public static void loadConfig() {
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("commands.arena.info");
        FORMAT = manager.get("format");
    }
}
