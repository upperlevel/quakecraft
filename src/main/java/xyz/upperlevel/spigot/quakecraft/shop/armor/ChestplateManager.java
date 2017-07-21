package xyz.upperlevel.spigot.quakecraft.shop.armor;

import lombok.Getter;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.shop.Purchase;
import xyz.upperlevel.spigot.quakecraft.shop.PurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.gui.config.itemstack.CustomItem;

public class ChestplateManager extends PurchaseManager<ChestplateManager.Chestplate> {

    @Override
    public Chestplate deserialize(String id, Config config) {
        return new Chestplate(id, config);
    }

    @Override
    public String getGuiLoc() {
        return "kits";
    }

    @Override
    public String getConfigLoc() {
        return "armor/kits";
    }

    @Override
    public void setSelected(QuakePlayer player, Chestplate purchase) {
        player.setSelectedChestplate(purchase);
    }

    @Override
    public Chestplate getSelected(QuakePlayer player) {
        return player.getSelectedChestplate();
    }

    @Override
    public String getPurchaseName() {
        return "chestplate";
    }

    @Getter
    public class Chestplate extends Purchase<Chestplate> {
        private final CustomItem item;

        public Chestplate(String id, String name, float cost, CustomItem icon, boolean def, CustomItem item) {
            super(ChestplateManager.this, id, name, cost, icon, def);
            this.item = item;
        }

        protected Chestplate(String id, Config config) {
            super(ChestplateManager.this, id, config);
            this.item = config.getCustomItem("item");
        }
    }
}
