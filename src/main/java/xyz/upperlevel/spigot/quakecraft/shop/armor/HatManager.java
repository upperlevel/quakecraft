package xyz.upperlevel.spigot.quakecraft.shop.armor;

import lombok.Getter;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.shop.Purchase;
import xyz.upperlevel.spigot.quakecraft.shop.PurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.gui.config.itemstack.CustomItem;

public class HatManager extends PurchaseManager<HatManager.Hat> {

    @Override
    public Hat deserialize(String id, Config config) {
        return new Hat(id, config);
    }

    @Override
    public String getGuiLoc() {
        return "hats";
    }

    @Override
    public String getConfigLoc() {
        return "armor/hats";
    }

    @Override
    public void setSelected(QuakePlayer player, Hat purchase) {
        player.setSelectedHat(purchase);
    }

    @Override
    public Hat getSelected(QuakePlayer player) {
        return player.getSelectedHat();
    }

    @Override
    public String getPurchaseName() {
        return "hat";
    }

    @Getter
    public class Hat extends Purchase<Hat> {
        private final CustomItem item;

        public Hat(String id, String name, float cost, CustomItem icon, boolean def, CustomItem item) {
            super(HatManager.this, id, name, cost, icon, def);
            this.item = item;
        }

        protected Hat(String id, Config config) {
            super(HatManager.this, id, config);
            this.item = config.getCustomItem("item");
        }
    }
}

