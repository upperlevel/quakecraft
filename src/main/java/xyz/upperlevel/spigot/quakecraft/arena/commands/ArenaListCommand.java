package xyz.upperlevel.spigot.quakecraft.arena.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.command.WithPermission;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

import java.util.Collection;

@WithPermission(value = "list", desc = "Allows you to get a list of all arenas")
public class ArenaListCommand extends Command {
    private static Message HEADER;
    private static Message LINE_ENABLED;
    private static Message LINE_DISABLED;
    private static Message LINE_UNREADY;
    private static Message FOOTER;
    private static Message EMPTY;

    public ArenaListCommand() {
        super("list");
        setDescription("Shows arenas list.");
    }

    @Executor
    public void run(CommandSender sender) {
        Collection<Arena> list = QuakeCraftReloaded.get().getArenaManager().getArenas();

        if (list.size() > 0) {
            HEADER.send(sender, "arenas", String.valueOf(list.size()));
            for(Arena arena : list) {
                Message message;
                if(QuakeCraftReloaded.get().getGameManager().getGame(arena) != null)
                    message = LINE_ENABLED;
                else if(arena.isReady())
                    message = LINE_DISABLED;
                else
                    message = LINE_UNREADY;
                message.send(sender, arena.getPlaceholders());
            }
            FOOTER.send(sender);
        } else
            EMPTY.send(sender);
    }

    public static void loadConfig() {
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("commands.arena.list");
        HEADER = manager.get("header");
        MessageManager line = manager.getSection("line");
        LINE_ENABLED = line.get("enabled");
        LINE_DISABLED = line.get("disabled");
        LINE_UNREADY = line.get("not-ready");
        FOOTER = manager.get("footer");
        EMPTY = manager.get("no-arena-found");
    }
}
