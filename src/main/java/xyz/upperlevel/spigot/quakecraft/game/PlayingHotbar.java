package xyz.upperlevel.spigot.quakecraft.game;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;
import xyz.upperlevel.uppercore.gui.Icon;
import xyz.upperlevel.uppercore.itemstack.CustomItem;
import xyz.upperlevel.uppercore.gui.hotbar.Hotbar;
import xyz.upperlevel.uppercore.itemstack.ItemResolver;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.util.List;

public class PlayingHotbar extends Hotbar {

    // GUN
    public static class Gun implements ItemResolver {
        private final PlaceholderValue<String> name;
        private final List<PlaceholderValue<String>> lore;

        public Gun(Config config) {
            name = config.getMessageRequired("name");
            lore = config.getMessageList("lore");
        }

        @Override
        public ItemStack resolve(Player player) {
            QuakePlayer quake = QuakeCraftReloaded.get().getPlayerManager().getPlayer(player);
            CustomItem item = quake.getSelectedCase().getItem();
            item.setDisplayName(name);
            item.setLore(lore);
            return item.resolve(player);
        }

        public static Gun deserialize(Config config) {
            try {
                return new Gun(config);
            } catch (InvalidConfigurationException e) {
                e.addLocalizer("in gun");
                throw e;
            }
        }
    }

    // TRACKER
    public static class Tracker implements ItemResolver {
        private final PlaceholderValue<String> name;
        private final List<PlaceholderValue<String>> lore;

        public Tracker(Config config) {
            name = config.getMessage("name");
            lore = config.getMessageList("lore");
        }

        @Override
        public ItemStack resolve(Player player) {
            CustomItem item = new CustomItem(new ItemStack(Material.COMPASS));
            item.setDisplayName(name);
            item.setLore(lore);
            return item.resolve(player);
        }

        public static Tracker deserialize(Config config) {
            try {
                return new Tracker(config);
            } catch (InvalidConfigurationException e) {
                e.addLocalizer("in tracker");
                throw e;
            }
        }
    }

    public PlayingHotbar(Plugin plugin, String id, Config config) {
        super(plugin, id, config);
        Config sub;
        sub = config.getConfigRequired("gun");
        setIcon(sub.getIntRequired("slot"), new Icon(Gun.deserialize(sub.getConfigRequired("item"))));

        sub = config.getConfigRequired("tracker");
        setIcon(sub.getIntRequired("slot"), new Icon(Tracker.deserialize(sub.getConfigRequired("item"))));
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
