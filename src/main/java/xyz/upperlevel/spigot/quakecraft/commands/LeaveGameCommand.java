package xyz.upperlevel.spigot.quakecraft.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.command.Sender;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

import java.util.Arrays;
import java.util.List;

public class LeaveGameCommand extends Command {
    private static Message NO_GAME;
    private static Message SUCCESS;

    public LeaveGameCommand() {
        super("leave");
        setDescription("Leaves from the arena joined.");
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("quit", "exit");
    }

    @Executor(sender = Sender.PLAYER)
    public void run(CommandSender sender) {
        Game game = QuakeCraftReloaded.get().getGameManager().getGame((Player) sender);
        if (game == null) {
            NO_GAME.send(sender);
            return;
        }
        game.leave((Player) sender);
        SUCCESS.send((Player) sender, "arena", game.getArena().getId());
    }

    public static void loadConfig() {
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("commands.leave");
        NO_GAME = manager.get("not-playing");
        SUCCESS = manager.get("success");
    }
}
