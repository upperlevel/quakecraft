package xyz.upperlevel.quakecraft.shop.armor;

import lombok.Getter;
import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.UItem;

public class ChestplateManager extends SinglePurchaseManager<ChestplateManager.Chestplate> {

    public ChestplateManager(PurchaseRegistry registry) {
        super(registry, "armor.kits");
    }

    @Override
    public Chestplate deserialize(String id, Config config) {
        return new Chestplate(id, config);
    }

    @Override
    public void setSelected(QuakeAccount player, Chestplate purchase) {
        player.setSelectedChestplate(purchase);
    }

    @Override
    public Chestplate getSelected(QuakeAccount player) {
        return player.getSelectedChestplate();
    }

    @Override
    public String getPurchaseName() {
        return "chestplate";
    }

    @Getter
    public class Chestplate extends SimplePurchase<Chestplate> {
        private final UItem item;

        protected Chestplate(String id, Config config) {
            super(ChestplateManager.this, id, config);
            this.item = config.getUItem("item", UItem.AIR);
        }
    }
}
