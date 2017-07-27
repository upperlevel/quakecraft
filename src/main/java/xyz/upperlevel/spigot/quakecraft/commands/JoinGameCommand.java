package xyz.upperlevel.spigot.quakecraft.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.command.Sender;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

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
        Game game = QuakeCraftReloaded.get().getGameManager().getGame((Player) sender);
        Player player = (Player) sender;
        if (game != null) {
            ALREADY_PLAYING.send(player);
            return;
        }
        game = QuakeCraftReloaded.get().getGameManager().getGame(arena);
        if (game == null) {
            NO_GAME_FOUND.send(player, "game", arena.getName());
            return;
        }
        game.join(player);
        SUCCESS.send(player, "game", game.getArena().getName());
    }

    public static void loadConfig() {
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("commands.join");
        ALREADY_PLAYING = manager.get("already-playing");
        NO_GAME_FOUND = manager.get("no-game-found");
        SUCCESS = manager.get("success");
    }
}
