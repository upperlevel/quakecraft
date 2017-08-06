package xyz.upperlevel.spigot.quakecraft.arena.commands.powerup;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

public class ArenaPowerupRemoveCommand extends Command {
    private static Message INVALID_INDEX;
    private static Message SUCCESS;

    public ArenaPowerupRemoveCommand() {
        super("remove");
        setDescription("Removes arena powerup.");
    }

    @Executor()
    public void run(CommandSender sender, @Argument("arena") Arena arena, @Argument("int") int index) {
        if(index <= 1 || index > arena.getPowerups().size()) {
            INVALID_INDEX.send(sender, "index", String.valueOf(index));
            return;
        }
        arena.getPowerups().remove(index - 1);
        SUCCESS.send((Player) sender, arena.getPlaceholders());
    }

    public static void loadConfig() {
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("commands.arena.powerup.remove");
        INVALID_INDEX = manager.get("invalid-index");
        SUCCESS = manager.get("success");
    }
}

