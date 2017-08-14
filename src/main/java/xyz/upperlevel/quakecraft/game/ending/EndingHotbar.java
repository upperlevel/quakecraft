package xyz.upperlevel.quakecraft.game.ending;

import org.bukkit.plugin.Plugin;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;
import xyz.upperlevel.uppercore.hotbar.Hotbar;

public class EndingHotbar extends Hotbar {

    public EndingHotbar(Plugin plugin, Config config) {
        super(plugin, config);
        // nothing new
    }

    public static EndingHotbar deserialize(Plugin plugin, Config config) {
        try {
            return new EndingHotbar(plugin, config);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in ending hotbar");
            throw e;
        }
    }
}
