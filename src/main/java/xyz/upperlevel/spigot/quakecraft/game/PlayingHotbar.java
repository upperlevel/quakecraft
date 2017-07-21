package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.gui.Icon;
import xyz.upperlevel.uppercore.gui.config.itemstack.CustomItem;
import xyz.upperlevel.uppercore.gui.hotbar.Hotbar;

@Getter
public class GameHotbar extends Hotbar {

    private CustomItem gun;
    private CustomItem tracker;

    public GameHotbar(Plugin plugin, String id, Config config) {
        super(plugin, id, config);
        // -----------------------gun
        if (config.has("gun")) {
            config = config.getConfig("gun");
            gun = config.getCustomItemRequired("item");
            setIcon(config.getIntRequired("slot"), new Icon(gun));
        } else
            QuakeCraftReloaded.get().getLogger().warning("Item \"gun\" not found in ingame hotbar");
        // -----------------------tracker
        if (config.has("tracker")) {
            config = config.getConfig("tracker");
            tracker = config.getCustomItemRequired("tracker");
            setIcon(config.getIntRequired("slot"), new Icon(tracker));
        } else
            QuakeCraftReloaded.get().getLogger().warning("Item \"tracker\" not found in ingame hotbar");
    }

    public static GameHotbar deserialize(Plugin plugin, String id, Config config) {
        return new GameHotbar(plugin, id, config);
    }
}
