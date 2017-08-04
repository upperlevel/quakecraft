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

@WithPermission(value = "create", desc = "Allows you to create a new arena")
public class ArenaCreateCommand extends Command {
    private static Message NAME_ALREADY_PRESENT;
    private static Message INVALID_NAME;
    private static Message SUCCESS;

    public ArenaCreateCommand() {
        super("create");
        setDescription( "Creates a new arena.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") String arenaId) {
        if (QuakeCraftReloaded.get().getArenaManager().getArena(arenaId) != null) {
            NAME_ALREADY_PRESENT.send(sender, "arena", arenaId);
            return;
        }
        if (!Arena.isValidName(arenaId)) {
            INVALID_NAME.send(sender, "arena", arenaId);
            return;
        }
        Arena arena = new Arena(arenaId);
        QuakeCraftReloaded.get().getArenaManager().addArena(arena);
        SUCCESS.send(sender, arena.getPlaceholders());
    }

    public static void loadConfig() {
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("commands.arena.create");
        NAME_ALREADY_PRESENT = manager.get("name-already-present");
        INVALID_NAME = manager.get("invalid-name");
        SUCCESS = manager.get("success");
    }
}
