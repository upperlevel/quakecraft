package xyz.upperlevel.spigot.quakecraft.arena.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

public class ArenaEnableCommand extends Command {
    private static Message NOT_READY;
    private static Message ALREADY_ENABLED;
    private static Message SUCCESS;

    public ArenaEnableCommand() {
        super("enable");
        setDescription("Enables an arena.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") Arena arena) {
        if (!arena.isReady()) {
            NOT_READY.send(sender, arena.getPlaceholders());
            return;
        }
        if (QuakeCraftReloaded.get().getGameManager().getGame(arena) != null) {
            ALREADY_ENABLED.send(sender, arena.getPlaceholders());
            return;
        }
        QuakeCraftReloaded.get().getGameManager().addGame(new Game(arena));
        SUCCESS.send(sender, arena.getPlaceholders());
    }

    public static void loadConfig() {
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("commands.arena.enable");
        NOT_READY = manager.get("arena-not-ready");
        ALREADY_ENABLED = manager.get("arena-already-enabled");
        SUCCESS = manager.get("success");
    }
}
