package xyz.upperlevel.spigot.quakecraft.arena.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

public class ArenaDeleteCommand extends Command {
    private static Message NOT_FOUND;
    private static Message SUCCESS;

    public ArenaDeleteCommand() {
        super("delete");
        setDescription("Deletes an arena.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") String arenaId) {
        if (QuakeCraftReloaded.get().getArenaManager().removeArena(arenaId) == null) {
            NOT_FOUND.send(sender, "arena", arenaId);
            return;
        }
        SUCCESS.send(sender, "arena", arenaId);
    }

    public static void loadConfig() {
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("commands.arena.delete");
        NOT_FOUND = manager.get("arena-not-found");
        SUCCESS = manager.get("success");
    }
}
