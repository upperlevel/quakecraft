package xyz.upperlevel.quakecraft.arena.commands;

import xyz.upperlevel.quakecraft.arena.commands.powerup.ArenaPowerupAddCommand;
import xyz.upperlevel.quakecraft.arena.commands.powerup.ArenaPowerupEffectsCommand;
import xyz.upperlevel.quakecraft.arena.commands.powerup.ArenaPowerupListCommand;
import xyz.upperlevel.quakecraft.arena.commands.powerup.ArenaPowerupRemoveCommand;
import xyz.upperlevel.uppercore.command.NodeCommand;

import static xyz.upperlevel.uppercore.util.CrashUtil.loadSafe;

public class ArenaPowerupCommand extends NodeCommand {
    public ArenaPowerupCommand() {
        super("powerup");

        register(new ArenaPowerupAddCommand());
        register(new ArenaPowerupEffectsCommand());
        register(new ArenaPowerupListCommand());
        register(new ArenaPowerupRemoveCommand());

        addAliases("pow", "pows");
        setDescription("Commands related to Powerups");
    }

    public static void loadConfig() {
        loadSafe("add", ArenaPowerupAddCommand::loadConfig);
        loadSafe("effects", ArenaPowerupEffectsCommand::loadConfig);
        loadSafe("list", ArenaPowerupListCommand::loadConfig);
        loadSafe("remove", ArenaPowerupRemoveCommand::loadConfig);
    }
}
