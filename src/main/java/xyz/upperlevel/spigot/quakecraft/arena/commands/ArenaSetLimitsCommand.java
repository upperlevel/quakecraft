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
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;

@WithPermission(value = "setlimits", desc = "Allows you to set an arena's limits")
public class ArenaSetLimitsCommand extends Command {
    private static Message MIN_MORE_MAX;
    private static Message SUCCESS;

    public ArenaSetLimitsCommand() {
        super("setlimits");
        setDescription("Sets arena min and max players count.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") Arena arena, @Argument("min") int min, @Argument("max") int max) {
        if (max < min) {
            PlaceholderRegistry reg = PlaceholderRegistry.create(arena.getPlaceholders());
            reg.set("min", String.valueOf(min));
            reg.set("max", String.valueOf(max));
            MIN_MORE_MAX.send(sender, reg);
            return;
        }
        arena.setLimits(min, max);
        SUCCESS.send(sender, arena.getPlaceholders());
    }

    public static void loadConfig() {
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("commands.arena.set-limits");
        MIN_MORE_MAX = manager.get("min-more-max");
        SUCCESS = manager.get("success");
    }
}
