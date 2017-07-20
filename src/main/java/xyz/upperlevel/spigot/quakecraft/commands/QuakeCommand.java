package xyz.upperlevel.spigot.quakecraft.commands;

import org.bukkit.command.CommandExecutor;
import xyz.upperlevel.uppercore.command.NodeCommand;

public class QuakeCommand extends NodeCommand implements CommandExecutor {

    public QuakeCommand() {
        super("quake");

        register(new CreateArenaCommand());
        register(new ArenaInfoCommand());
        register(new DeleteArenaCommand());
        register(new JoinGameCommand());
        register(new LeaveGameCommand());
        register(new SetDisplayNameCommand());
        register(new ArenaListCommand());
        register(new SetLobbyCommand());
        register(new SetLimitsCommand());
        register(new SaveArenaCommand());
        register(new AddSpawnCommand());
        register(new EnableArenaCommand());
        register(new DisableArenaCommand());
        register(new ShopCommand());

        setDescription("Main commands of QuakeReloaded plugin.");
        addAliases("quakecraft", "quakecraftreloaded");
    }
}
