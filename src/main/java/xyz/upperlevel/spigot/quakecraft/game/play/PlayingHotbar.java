package xyz.upperlevel.spigot.quakecraft.game.play;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;
import xyz.upperlevel.uppercore.gui.Icon;
import xyz.upperlevel.uppercore.itemstack.CustomItem;
import xyz.upperlevel.uppercore.itemstack.ItemResolver;
import xyz.upperlevel.uppercore.hotbar.Hotbar;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.util.List;

public class PlayingHotbar extends Hotbar {

    public PlayingHotbar(Config config) {
        super(QuakeCraftReloaded.get(), config);
        Config sub;
        sub = config.getConfigRequired("gun");
        setIcon(sub.getIntRequired("slot"), new Icon(Gun.deserialize(sub.getConfigRequired("item"))));

        sub = config.getConfigRequired("tracker");
        setIcon(sub.getIntRequired("slot"), new Icon(Tracker.deserialize(sub.getConfigRequired("item"))));
    }

    // GUN
    public static class Gun implements ItemResolver {
        private final PlaceholderValue<String> name;
        private final List<PlaceholderValue<String>> lore;

        public Gun(Config config) {
            name = config.getMessageStrRequired("name");
            lore = config.getMessageStrList("lore");
        }

        @Override
        public ItemStack resolve(Player player) {
            QuakePlayer quake = QuakeCraftReloaded.get().getPlayerManager().getPlayer(player);
            CustomItem item = quake.getSelectedCase().getItem();
            item.setDisplayName(name);
            item.setLore(lore);
            item.setPlaceholders(PlaceholderRegistry.create()
                    .set("case", quake.getSelectedCase().getName().resolve(player))
                    .set("laser", quake.getSelectedLaser().getName().resolve(player))
                    .set("barrel", quake.getSelectedBarrel().getName().resolve(player))
                    .set("muzzle", quake.getSelectedMuzzle().getName().resolve(player))
                    .set("trigger", quake.getSelectedTrigger().getName().resolve(player)));
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
            name = config.getMessageStr("name");
            lore = config.getMessageStrList("lore");
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

    public static PlayingHotbar deserialize(String id, Config config) {
        try {
            return new PlayingHotbar(config);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in hotbar " + id);
            throw e;
        }
    }
}
