package xyz.upperlevel.quakecraft.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.game.Game;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.command.Sender;
import xyz.upperlevel.uppercore.command.WithPermission;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

import java.util.Arrays;
import java.util.List;

@WithPermission(value = "leave", desc = "Allows you to leave an arena")
public class LeaveGameCommand extends Command {
    public static final List<String> ALIASES = Arrays.asList("leave", "quit", "exit");

    private static Message NO_GAME;
    private static Message SUCCESS;

    public LeaveGameCommand() {
        super(ALIASES.get(0));
        setDescription("Leaves from the arena joined.");
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList(ALIASES.get(1), ALIASES.get(2));
    }

    @Executor(sender = Sender.PLAYER)
    public void run(CommandSender sender) {
        Game game = Quakecraft.get().getGameManager().getGame((Player) sender);
        if (game == null) {
            NO_GAME.send(sender);
            return;
        }
        if (game.isJoinCooldownOver((Player)sender)) {
            game.leave((Player) sender);
            SUCCESS.send((Player) sender, game.getPlaceholders());
        }
    }

    public static void loadConfig() {
        MessageManager manager = Quakecraft.get().getMessages().getSection("commands.leave");
        NO_GAME = manager.get("not-playing");
        SUCCESS = manager.get("success");
    }
}
