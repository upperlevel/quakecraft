package xyz.upperlevel.spigot.quakecraft.arena.commands.spawn;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.*;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

@WithPermission(value = "remove", desc = "Allows you to remove a spawn from an arena")
public class ArenaSpawnRemoveCommand extends Command {
    private static Message INVALID_INDEX;
    private static Message SUCCESS;

    public ArenaSpawnRemoveCommand() {
        super("remove");
        setDescription("Removes arena spawn.");
    }

    @Executor(sender = Sender.PLAYER)
    public void run(CommandSender sender, @Argument("arena") Arena arena, @Argument("int") int index) {
        if(index <= 1 || index > arena.getSpawns().size()) {
            INVALID_INDEX.send(sender, "index", String.valueOf(index));
            return;
        }
        arena.getSpawns().remove(index - 1);
        SUCCESS.send((Player) sender, arena.getPlaceholders());
    }

    public static void loadConfig() {
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("commands.arena.spawn.remove");
        INVALID_INDEX = manager.get("invalid-index");
        SUCCESS = manager.get("success");
    }
}

