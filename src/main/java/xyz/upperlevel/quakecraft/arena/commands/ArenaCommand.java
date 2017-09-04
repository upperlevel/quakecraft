package xyz.upperlevel.quakecraft.arena.commands;

import xyz.upperlevel.uppercore.command.NodeCommand;
import xyz.upperlevel.uppercore.command.WithChildPermission;
import xyz.upperlevel.uppercore.command.WithPermission;

import static xyz.upperlevel.uppercore.command.DefaultPermission.OP;
import static xyz.upperlevel.uppercore.util.CrashUtil.loadSafe;

@WithPermission(value = "arena", def = OP)
@WithChildPermission(def = OP, desc = "Gives access to all the arena commands")
public class ArenaCommand extends NodeCommand {

    public ArenaCommand() {
        super("arena");

        register(new ArenaSetupGuiCommand());
        register(new ArenaSpawnCommand());
        register(new ArenaPowerupCommand());
        register(new ArenaCreateCommand());
        register(new ArenaDeleteCommand());
        register(new ArenaDisableCommand());
        register(new ArenaEnableCommand());
        register(new ArenaInfoCommand());
        register(new ArenaListCommand());
        register(new ArenaSetLimitsCommand());
        register(new ArenaSetLobbyCommand());
        register(new ArenaSetNameCommand());
        register(new SetKillsToWinCommand());
        register(new ArenaSetHideNametagsCommand());
        register(new ArenaSetSneakCommand());

        setDescription("Commands for arena editing.");
        addAlias("map");
    }

    public static void loadConfig() {
        loadSafe("spawn", ArenaSpawnCommand::loadConfig);
        loadSafe("item-box", ArenaPowerupCommand::loadConfig);
        loadSafe("create", ArenaCreateCommand::loadConfig);
        loadSafe("delete", ArenaDeleteCommand::loadConfig);
        loadSafe("disable", ArenaDisableCommand::loadConfig);
        loadSafe("enable", ArenaEnableCommand::loadConfig);
        loadSafe("list", ArenaListCommand::loadConfig);
        loadSafe("info", ArenaInfoCommand::loadConfig);
        loadSafe("set-limits", ArenaSetLimitsCommand::loadConfig);
        loadSafe("set-lobby", ArenaSetLobbyCommand::loadConfig);
        loadSafe("set-name", ArenaSetNameCommand::loadConfig);
        loadSafe("set-kills-to-win", SetKillsToWinCommand::loadConfig);
    }
}
