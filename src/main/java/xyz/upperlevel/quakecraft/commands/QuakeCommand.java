package xyz.upperlevel.quakecraft.commands;

import org.bukkit.command.CommandExecutor;
import xyz.upperlevel.quakecraft.arena.commands.*;
import xyz.upperlevel.quakecraft.game.commands.SignCmd;
import xyz.upperlevel.uppercore.command.NodeCommand;
import xyz.upperlevel.uppercore.command.WithChildPermission;
import xyz.upperlevel.uppercore.command.WithPermission;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;

import static xyz.upperlevel.uppercore.util.CrashUtil.loadSafe;

@WithPermission("quakecraft")
@WithChildPermission(desc = "Gives access to all quakecraft commands")
public class QuakeCommand extends NodeCommand implements CommandExecutor {

    public QuakeCommand() {
        super("quake");

        register(new JoinGameCommand());
        register(new LeaveGameCommand());
        register(new ShopCommand());
        register(new ArenaCommand());
        register(new LobbyCommand());
        register(new SignCmd());

        setDescription("Main commands of Quakecraft plugin.");
        addAliases("quakecraft");
    }

    public static void loadConfig() {
        try {
            loadSafe("join", JoinGameCommand::loadConfig);
            loadSafe("leave", LeaveGameCommand::loadConfig);
            loadSafe("arena", ArenaCommand::loadConfig);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in commands messages");
            throw e;
        }
    }
}