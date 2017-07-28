package xyz.upperlevel.spigot.quakecraft.arena.commands;

import xyz.upperlevel.uppercore.command.NodeCommand;

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
        ArenaAddSpawnCommand.loadConfig();
        ArenaCreateCommand.loadConfig();
        ArenaDeleteCommand.loadConfig();
        ArenaDisableCommand.loadConfig();
        ArenaEnableCommand.loadConfig();
        ArenaListCommand.loadConfig();
        ArenaInfoCommand.loadConfig();
        ArenaSetLimitsCommand.loadConfig();
        ArenaSetLobbyCommand.loadConfig();
        ArenaSetNameCommand.loadConfig();
        SetKillsToWinCommand.loadConfig();
    }
}
