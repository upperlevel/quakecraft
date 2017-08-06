package xyz.upperlevel.quakecraft.arena.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.command.WithPermission;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

@WithPermission(value = "disable", desc = "Allows you to disable an arena")
public class ArenaDisableCommand extends Command {
    private static Message ALREADY_DISABLED;
    private static Message SUCCESS;

    public ArenaDisableCommand() {
        super("disable");
        setDescription("Disables an arena.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") Arena arena) {
        if (Quakecraft.get().getGameManager().removeGame(arena) == null) {
            ALREADY_DISABLED.send(sender, arena.getPlaceholders());
            return;
        }
        SUCCESS.send(sender, arena.getPlaceholders());
    }

    public static void loadConfig() {
        MessageManager manager = Quakecraft.get().getMessages().getSection("commands.arena.disable");
        ALREADY_DISABLED = manager.get("arena-already-disabled");
        SUCCESS = manager.get("success");
    }
}
