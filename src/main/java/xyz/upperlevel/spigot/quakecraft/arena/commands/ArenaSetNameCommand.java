package xyz.upperlevel.spigot.quakecraft.arena.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

public class ArenaSetNameCommand extends Command {
    private static Message SUCCESS;

    public ArenaSetNameCommand() {
        super("setname");
        setDescription("Sets arena display name.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") Arena arena, @Argument("name") String[] name) {
        String result = String.join(" ", name);
        arena.setName(result);
        SUCCESS.send(sender, "arena", arena.getId(), "name", arena.getName());
    }

    public static void loadConfig() {
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("commands.arena.set-name");
        SUCCESS = manager.get("success");
    }
}
