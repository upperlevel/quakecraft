package xyz.upperlevel.spigot.quakecraft.commands;

import org.bukkit.command.CommandExecutor;
import xyz.upperlevel.spigot.quakecraft.arena.commands.*;
import xyz.upperlevel.uppercore.command.NodeCommand;

public class QuakeCommand extends NodeCommand implements CommandExecutor {

    public QuakeCommand() {
        super("quake");

        register(new JoinGameCommand());
        register(new LeaveGameCommand());
        register(new ShopCommand());
        register(new ArenaCommand());

        setDescription("Main commands of QuakeReloaded plugin.");
        addAliases("quakecraft", "quakecraftreloaded");
    }

    public static void loadConfig() {
        JoinGameCommand.loadConfig();
        LeaveGameCommand.loadConfig();
        ArenaCommand.loadConfig();
    }
}
