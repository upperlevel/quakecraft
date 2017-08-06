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

@WithPermission(value = "setname", desc = "Allows you to set an arena's name")
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
        SUCCESS.send(sender, arena.getPlaceholders());
    }

    public static void loadConfig() {
        MessageManager manager = Quakecraft.get().getMessages().getSection("commands.arena.set-name");
        SUCCESS = manager.get("success");
    }
}
