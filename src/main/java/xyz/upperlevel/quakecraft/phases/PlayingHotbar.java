package xyz.upperlevel.quakecraft.phases;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.ConfigConstructor;
import xyz.upperlevel.uppercore.config.ConfigProperty;
import xyz.upperlevel.uppercore.gui.ConfigIcon;
import xyz.upperlevel.uppercore.hotbar.Hotbar;
import xyz.upperlevel.uppercore.itemstack.ItemResolver;
import xyz.upperlevel.uppercore.itemstack.UItem;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.util.List;

public class PlayingHotbar extends Hotbar {
    @ConfigConstructor
    public PlayingHotbar(Config config) {
        super(Quake.get(), config);

        Config sub = config.getConfigRequired("gun");
        setIcon(sub.getIntRequired("slot"), new ConfigIcon(sub.getRequired("item", Gun.class)));

        sub = config.getConfigRequired("tracker");
        setIcon(sub.getIntRequired("slot"), new ConfigIcon(sub.getRequired("item", Tracker.class)));
    }

    public static class Gun implements ItemResolver {
        private final PlaceholderValue<String> name;
        private final List<PlaceholderValue<String>> lore;

        @ConfigConstructor
        public Gun(
                @ConfigProperty("name") PlaceholderValue<String> name,
                @ConfigProperty("lore") List<PlaceholderValue<String>> lore) {
            this.name = name;
            this.lore = lore;
        }

        @Override
        public ItemStack resolve(Player player) {
            // The gun is particular: name and lore are both configurable but can have some
            // extra placeholders, such as gun's components name.
            QuakeAccount quake = Quake.get().getPlayerManager().getAccount(player);
            UItem item = quake.getSelectedCase().getItem();
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
    }

    public static class Tracker implements ItemResolver {
        private final PlaceholderValue<String> name;
        private final List<PlaceholderValue<String>> lore;

        @ConfigConstructor
        public Tracker(
                @ConfigProperty("name") PlaceholderValue<String> name,
                @ConfigProperty("lore") List<PlaceholderValue<String>> lore) {
            this.name = name;
            this.lore = lore;
        }

        @Override
        public ItemStack resolve(Player player) {
            UItem item = new UItem(new ItemStack(Material.COMPASS));
            item.setDisplayName(name);
            item.setLore(lore);
            return item.resolve(player);
        }
    }
}
