package xyz.upperlevel.quakecraft.shop.armor;

import lombok.Getter;
import xyz.upperlevel.quakecraft.QuakePlayer;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.CustomItem;

public class ChestplateManager extends SinglePurchaseManager<ChestplateManager.Chestplate> {

    public ChestplateManager(PurchaseRegistry registry) {
        super(registry);
    }

    @Override
    public Chestplate deserialize(String id, Config config) {
        return new Chestplate(id, config);
    }

    @Override
    public String getGuiLoc() {
        return "armor/kits/kits_gui";
    }

    @Override
    public String getConfigLoc() {
        return "armor/kits/kits";
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
    public class Chestplate extends SimplePurchase<Chestplate> {
        private final CustomItem item;

        protected Chestplate(String id, Config config) {
            super(ChestplateManager.this, id, config);
            this.item = config.getCustomItem("item", CustomItem.AIR);
        }
    }
}
