package xyz.upperlevel.spigot.quakecraft.arena.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

public class ArenaDisableCommand extends Command {
    private static Message ALREADY_DISABLED;
    private static Message SUCCESS;

    public ArenaDisableCommand() {
        super("disable");
        setDescription("Disables an arena.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") Arena arena) {
        if (QuakeCraftReloaded.get().getGameManager().removeGame(arena) == null) {
            ALREADY_DISABLED.send(sender, "arena", arena.getId());
            return;
        }
        SUCCESS.send(sender, "arena", arena.getId());
    }

    public static void loadConfig() {
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("commands.arena.disable");
        ALREADY_DISABLED = manager.get("arena-already-disabled");
        SUCCESS = manager.get("success");
    }
}
