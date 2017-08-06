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

@WithPermission(value = "delete", desc = "Allows you to delete an arena")
public class ArenaDeleteCommand extends Command {
    private static Message NOT_FOUND;
    private static Message SUCCESS;

    public ArenaDeleteCommand() {
        super("delete");
        setDescription("Deletes an arena.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") String arenaId) {
        Arena arena = Quakecraft.get().getArenaManager().removeArena(arenaId);
        if (arena == null) {
            NOT_FOUND.send(sender, "arena", arenaId);
            return;
        }
        SUCCESS.send(sender, arena.getPlaceholders());
    }

    public static void loadConfig() {
        MessageManager manager = Quakecraft.get().getMessages().getSection("commands.arena.delete");
        NOT_FOUND = manager.get("arena-not-found");
        SUCCESS = manager.get("success");
    }
}
