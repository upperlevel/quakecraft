package xyz.upperlevel.quakecraft.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.quakecraft.arena.QuakeArenaCommands;
import xyz.upperlevel.uppercore.arena.ArenaCommands;
import xyz.upperlevel.uppercore.command.NodeCommand;
import xyz.upperlevel.uppercore.command.PermissionUser;
import xyz.upperlevel.uppercore.command.SenderType;
import xyz.upperlevel.uppercore.command.functional.AsCommand;
import xyz.upperlevel.uppercore.command.functional.FunctionalCommand;
import xyz.upperlevel.uppercore.command.functional.WithPermission;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.gui.Gui;
import xyz.upperlevel.uppercore.placeholder.message.Message;

import static org.bukkit.ChatColor.RED;
import static xyz.upperlevel.uppercore.Uppercore.guis;

public class QuakeCommand extends NodeCommand {
    private static Message ARENA_NOT_FOUND;
    private static Message ARENA_JOINED;
    private static Message ARENA_QUIT;

    public QuakeCommand() {
        super("quake");

        description("Main commands of Quakecraft plugin.");
        aliases("q");

        // Registers all command used for arena editing, either from the game-api or not.
        FunctionalCommand.inject(this, new ArenaCommands(Quake.get().getArenaManager(), QuakeArena::new));
        FunctionalCommand.inject(this, new QuakeArenaCommands());

        // Registers must used commands as locals: such as join, quit, shop...
        FunctionalCommand.inject(this, this);
    }

    /* quake join */

    @AsCommand(
            description = "Join an arena.",
            sender = SenderType.PLAYER
    )
    @WithPermission(
            user = PermissionUser.AVAILABLE
    )
    protected void join(CommandSender sender, QuakeArena arena) {
        Player player = (Player) sender;
        if (arena == null) {
            ARENA_NOT_FOUND.send(player);
            return;
        }
        // todo if arena is already playing joins in spectator mode
        if (arena.join(player)) {
            ARENA_JOINED.send(player, arena.getPlaceholderRegistry());
        }
    }

    /* quake leave */

    @AsCommand(
            description = "Leave from the arena joined.",
            sender = SenderType.PLAYER,
            aliases = {"quit", "exit"}
    )
    @WithPermission(
            user = PermissionUser.AVAILABLE
    )
    protected void leave(CommandSender sender) {
        QuakeArena arena = (QuakeArena) Quake.get().getArenaManager().getArena((Player) sender);
        if (arena == null) {
            ARENA_NOT_FOUND.send(sender);
            return;
        }
        arena.quit((Player) sender);
        ARENA_QUIT.send((Player) sender);
    }

    /* quake shop */

    @AsCommand(
            description = "Open the shop GUI.",
            sender = SenderType.PLAYER
    )
    protected void shop(CommandSender sender) {
        Gui gui = Quake.get().getGuis().get("shop_gui");
        if (gui == null) {
            sender.sendMessage(RED + "shop_gui not found.");
            return;
        }
        guis().open((Player) sender, gui);
    }

    public static void loadConfig() {
        Config mex = Quake.getConfigSection("messages.commands.arena");
        ARENA_NOT_FOUND = mex.getMessageRequired("join.arena-not-found");
        ARENA_JOINED = mex.getMessageRequired("join.success");
        ARENA_QUIT = mex.getMessageRequired("leave.success");
    }
}
