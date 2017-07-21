package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;
import xyz.upperlevel.uppercore.gui.Icon;
import xyz.upperlevel.uppercore.gui.config.itemstack.CustomItem;
import xyz.upperlevel.uppercore.gui.hotbar.Hotbar;

@Getter
public class PlayingHotbar extends Hotbar {

    private int gunSlot = -1;
    private CustomItem gun;

    private int trackerSlot = -1;
    private CustomItem tracker;

    public PlayingHotbar(Plugin plugin, String id, Config config) {
        super(plugin, id, config);
        // -----------------------gun
        if (config.has("gun")) {
            config = config.getConfig("gun");
            gun = config.getCustomItemRequired("item");
            setIcon(config.getIntRequired("slot"), new Icon(gun));
        } else
            throw new InvalidConfigurationException("The \"gun\" field cannot be found!");
        // -----------------------tracker
        if (config.has("tracker")) {
            config = config.getConfig("tracker");
            tracker = config.getCustomItemRequired("tracker");
            setIcon(config.getIntRequired("slot"), new Icon(tracker));
        } else
            QuakeCraftReloaded.get().getLogger().warning("Item \"tracker\" not found in ingame hotbar");
    }

    public static PlayingHotbar deserialize(Plugin plugin, String id, Config config) {
        try {
            return new PlayingHotbar(plugin, id, config);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in hotbar " + id);
            throw e;
        }
    }
}
