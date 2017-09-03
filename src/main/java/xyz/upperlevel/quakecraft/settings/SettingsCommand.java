package xyz.upperlevel.quakecraft.settings;

import xyz.upperlevel.uppercore.command.NodeCommand;
import xyz.upperlevel.uppercore.command.WithChildPermission;
import xyz.upperlevel.uppercore.command.WithPermission;

import static xyz.upperlevel.uppercore.command.DefaultPermission.TRUE;
import static xyz.upperlevel.uppercore.util.CrashUtil.loadSafe;

@WithPermission(value = "settings", def = TRUE)
@WithChildPermission(desc = "Gives access to all the settings")
public class SettingsCommand extends NodeCommand {
    public SettingsCommand() {
        super("settings");
        
        register(new ToggleTrakingCommand());
        register(new ToggleParticlesCommand());

        setDescription("Commands for disabling or enabling certain features.");
        addAlias("config");
        addAlias("conf");
    }

    public static void loadConfig() {
        loadSafe("particles", ToggleParticlesCommand::loadConfig);
        loadSafe("tracking", ToggleTrakingCommand::loadConfig);
    }
}
