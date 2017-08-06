package xyz.upperlevel.quakecraft.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.arena.Arena;
import xyz.upperlevel.quakecraft.game.Game;
import xyz.upperlevel.uppercore.command.*;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

@WithPermission(value = "join", desc = "Allows you to join an arena")
public class JoinGameCommand extends Command {
    private static Message ALREADY_PLAYING;
    private static Message NO_GAME_FOUND;
    private static Message SUCCESS;

    public JoinGameCommand() {
        super("join");
        setDescription("Joins an arena.");
    }

    @Executor(sender = Sender.PLAYER)
    public void run(CommandSender sender, @Argument("arena") Arena arena) {
        Player player = (Player) sender;
        Game game = Quakecraft.get().getGameManager().getGame(player);
        if (game != null) {
            ALREADY_PLAYING.send(player, game.getPlaceholders());
            return;
        }
        game = Quakecraft.get().getGameManager().getGame(arena);
        if (game == null) {
            NO_GAME_FOUND.send(player, "game", arena.getName());
            return;
        }
        game.join(player);
        SUCCESS.send(player, game.getPlaceholders());
    }

    public static void loadConfig() {
        MessageManager manager = Quakecraft.get().getMessages().getSection("commands.join");
        ALREADY_PLAYING = manager.get("already-playing");
        NO_GAME_FOUND = manager.get("no-game-found");
        SUCCESS = manager.get("success");
    }
}
