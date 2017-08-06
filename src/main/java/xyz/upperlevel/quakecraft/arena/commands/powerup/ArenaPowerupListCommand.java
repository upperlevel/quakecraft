package xyz.upperlevel.spigot.quakecraft.arena.commands.powerup;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.spigot.quakecraft.powerup.Powerup;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.command.Sender;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;

import java.util.List;

import static xyz.upperlevel.uppercore.util.LocUtil.format;

public class ArenaPowerupListCommand extends Command {
    private static Message HEADER;
    private static Message LINE;
    private static Message FOOTER;
    private static Message EMPTY;

    public ArenaPowerupListCommand() {
        super("list");
        setDescription("Shows all arena powerups.");
    }

    @Executor(sender = Sender.PLAYER)
    public void run(CommandSender sender, @Argument("arena") Arena arena) {
        List<Powerup> boxes = arena.getPowerups();
        int len = boxes.size();
        if (len > 0) {
            PlaceholderRegistry hfReg = PlaceholderRegistry.create(arena.getPlaceholders());
            hfReg.set("boxes", len);
            HEADER.send(sender, hfReg);

            PlaceholderRegistry reg = PlaceholderRegistry.create(arena.getPlaceholders());
            reg.set("boxes", len);

            for(int i = 0; i < len; i++) {
                Powerup box = boxes.get(i);
                reg.set("index", i + 1);
                reg.set("effect", box.getEffect().getId());
                reg.set("location", format(box.getLocation(), true));
                reg.set("respawn_ticks", box.getRespawnTicks());
                LINE.send(sender, reg);
            }

            FOOTER.send(sender, hfReg);
        } else {
            EMPTY.send(sender);
        }
    }

    public static void loadConfig() {
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("commands.arena.powerup.list");
        HEADER = manager.get("header");
        LINE = manager.get("line");
        FOOTER = manager.get("footer");
        EMPTY = manager.get("no-box-found");
    }
}

