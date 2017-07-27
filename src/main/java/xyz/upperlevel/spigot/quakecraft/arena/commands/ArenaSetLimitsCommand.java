package xyz.upperlevel.spigot.quakecraft.arena.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

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
            MIN_MORE_MAX.send(sender, "arena", arena.getId(), "min", String.valueOf(min), "max", String.valueOf(max));
            return;
        }
        arena.setLimits(min, max);
        SUCCESS.send(sender, "arena", arena.getId());
    }

    public static void loadConfig() {
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("commands.arena.set-limits");
        MIN_MORE_MAX = manager.get("min-more-max");
        SUCCESS = manager.get("success");
    }
}
