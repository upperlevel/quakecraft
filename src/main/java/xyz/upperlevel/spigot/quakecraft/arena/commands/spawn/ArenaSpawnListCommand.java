package xyz.upperlevel.spigot.quakecraft.arena.commands.spawn;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.*;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;

import java.util.List;

import static xyz.upperlevel.uppercore.util.LocUtil.format;

@WithPermission(value = "list", desc = "Allows you to list all spawns in an arena")
public class ArenaSpawnListCommand extends Command {
    private static Message HEADER;
    private static Message LINE;
    private static Message FOOTER;
    private static Message EMPTY;

    public ArenaSpawnListCommand() {
        super("list");
        setDescription("Shows all arena spawns.");
    }

    @Executor(sender = Sender.PLAYER)
    public void run(CommandSender sender, @Argument("arena") Arena arena) {
        List<Location> spawns = arena.getSpawns();
        int len = spawns.size();
        if (len > 0) {
            PlaceholderRegistry hfReg = PlaceholderRegistry.create(arena.getPlaceholders());
            hfReg.set("spawns", len);
            HEADER.send(sender, hfReg);

            PlaceholderRegistry reg = PlaceholderRegistry.create(arena.getPlaceholders());
            reg.set("spawns", len);

            for(int i = 0; i < len; i++) {
                reg.set("index", i + 1);
                reg.set("loc", format(spawns.get(i), true));
                LINE.send(sender, reg);
            }

            FOOTER.send(sender, hfReg);
        } else {
            EMPTY.send(sender);
        }
    }

    public static void loadConfig() {
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("commands.arena.spawn.list");
        HEADER = manager.get("header");
        LINE = manager.get("line");
        FOOTER = manager.get("footer");
        EMPTY = manager.get("no-spawn-found");
    }
}

