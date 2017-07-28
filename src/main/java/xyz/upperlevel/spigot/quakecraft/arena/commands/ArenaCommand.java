package xyz.upperlevel.spigot.quakecraft.arena.commands;

import xyz.upperlevel.uppercore.command.NodeCommand;

import static xyz.upperlevel.uppercore.util.CrashUtil.loadSafe;

public class ArenaCommand extends NodeCommand {

    public ArenaCommand() {
        super("arena");

        register(new ArenaAddSpawnCommand());
        register(new ArenaCreateCommand());
        register(new ArenaDeleteCommand());
        register(new ArenaDisableCommand());
        register(new ArenaEnableCommand());
        register(new ArenaInfoCommand());
        register(new ArenaListCommand());
        register(new ArenaSetLimitsCommand());
        register(new ArenaSetLobbyCommand());
        register(new ArenaSetNameCommand());
        register(new ArenaSetupGuiCommand());
        register(new SetKillsToWinCommand());

        setDescription("Commands for arena editing.");
        addAlias("map");
    }

    public static void loadConfig() {
        loadSafe("add-spawn", ArenaAddSpawnCommand::loadConfig);
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
