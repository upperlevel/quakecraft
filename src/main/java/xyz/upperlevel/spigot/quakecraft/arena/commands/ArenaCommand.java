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

        setDescription("Commands for arena editing.");
        addAlias("map");
    }
}
