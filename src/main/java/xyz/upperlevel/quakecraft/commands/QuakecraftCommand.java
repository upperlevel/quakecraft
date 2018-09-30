package xyz.upperlevel.quakecraft.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.NodeCommand;
import xyz.upperlevel.uppercore.command.PermissionUser;
import xyz.upperlevel.uppercore.command.SenderType;
import xyz.upperlevel.uppercore.command.functional.AsCommand;
import xyz.upperlevel.uppercore.command.functional.FunctionalCommand;
import xyz.upperlevel.uppercore.command.functional.WithPermission;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigException;
import xyz.upperlevel.uppercore.placeholder.message.Message;
import xyz.upperlevel.uppercore.placeholder.message.MessageManager;

import java.util.Arrays;
import java.util.List;

import static xyz.upperlevel.uppercore.Uppercore.guis;
import static xyz.upperlevel.uppercore.util.CrashUtil.loadSafe;

public class QuakecraftCommand extends NodeCommand {
    private static Message ALREADY_PLAYING;
    private static Message NO_GAME_FOUND;

    private static Message SUCCESS;
    private static Message NO_GAME;

    public static final List<String> ALIASES = Arrays.asList("quake", "quakecraft");

    public QuakecraftCommand() {
        super("quakecraft");

        description("Main commands of Quakecraft plugin.");
        aliases("quake");

        append(
                new ArenaCommand(),
                new LobbyCommand()
        );
        FunctionalCommand.inject(this, this);
    }

    // --------------------------------------------------------------------------------- quake join

    @AsCommand(
            description = "Join an arena.",
            sender = SenderType.PLAYER
    )
    @WithPermission(
            user = PermissionUser.AVAILABLE
    )
    protected void join(CommandSender sender, Arena arena) {
        Player player = (Player) sender;
        Game game = Quake.get().getGameManager().getGame(player);
        if (game != null) {
            ALREADY_PLAYING.send(player, game.getPlaceholders());
            return;
        }
        game = Quake.get().getGameManager().getGame(arena);
        if (game == null) {
            NO_GAME_FOUND.send(player, "game", arena.getId());
            return;
        }
        if (game.join(player)) {
            SUCCESS.send(player, game.getPlaceholders());
        }
    }

    // --------------------------------------------------------------------------------- quake leave

    @AsCommand(
            description = "Leave from the arena joined.",
            sender = SenderType.PLAYER,
            aliases = {"quit", "exit"}
    )
    @WithPermission(
            user = PermissionUser.AVAILABLE
    )
    protected void leave(CommandSender sender) {
        Game game = Quake.get().getGameManager().getGame((Player) sender);
        if (game == null) {
            NO_GAME.send(sender);
            return;
        }
        game.leave((Player) sender);
        SUCCESS.send((Player) sender, game.getPlaceholders());
    }

    // --------------------------------------------------------------------------------- quake shop

    @AsCommand(
            description = "Open the shop GUI.",
            sender = SenderType.PLAYER
    )
    protected void shop(CommandSender sender) {
        GuiId gui = Quake.get().getGuis().get("shop_gui");
        if (gui == null) {
            NO_GUI.send((Player) sender);
            return;
        }
        guis().open((Player) sender, gui.get());
    }

    public static void loadConfig() {
        try {
            loadSafe("join", JoinGameCommand::loadConfig);
            loadSafe("leave", LeaveGameCommand::loadConfig);
            loadSafe("arena", ArenaCommand::loadConfig);
        } catch (InvalidConfigException e) {
            e.addLocation("in commands messages");
            throw e;
        }
        MessageManager manager = Quake.get().getMessages().getSection("commands.join");
        ALREADY_PLAYING = manager.get("already-playing");
        NO_GAME_FOUND = manager.get("no-game-found");

        SUCCESS = manager.get("success");
        NO_GAME = manager.get("not-playing");
    }
}
