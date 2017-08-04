package xyz.upperlevel.spigot.quakecraft.arena.commands;

import xyz.upperlevel.spigot.quakecraft.arena.commands.spawn.*;
import xyz.upperlevel.uppercore.command.NodeCommand;
import xyz.upperlevel.uppercore.command.WithChildPermission;
import xyz.upperlevel.uppercore.command.WithPermission;

import static xyz.upperlevel.uppercore.util.CrashUtil.loadSafe;

@WithPermission("spawn")
@WithChildPermission(desc = "Allows you to manage arena's spawns")
public class ArenaSpawnCommand extends NodeCommand {
    public ArenaSpawnCommand() {
        super("spawn");

        register(new ArenaSpawnAddCommand());
        register(new ArenaSpawnListCommand());
        register(new ArenaSpawnRemoveCommand());

        setDescription("Spawn related commands");
    }


    public static void loadConfig() {
        loadSafe("add", ArenaSpawnAddCommand::loadConfig);
        loadSafe("list", ArenaSpawnListCommand::loadConfig);
        loadSafe("remove", ArenaSpawnRemoveCommand::loadConfig);
    }
}
