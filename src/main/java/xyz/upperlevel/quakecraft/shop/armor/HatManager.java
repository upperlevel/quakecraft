package xyz.upperlevel.quakecraft.shop.armor;

import lombok.Getter;
import xyz.upperlevel.quakecraft.QuakePlayer;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.CustomItem;

public class HatManager extends SinglePurchaseManager<HatManager.Hat> {

    public HatManager(PurchaseRegistry registry) {
        super(registry);
    }

    @Override
    public Hat deserialize(String id, Config config) {
        return new Hat(id, config);
    }

    @Override
    public String getGuiLoc() {
        return "armor/hats/hats_gui";
    }

    @Override
    public String getConfigLoc() {
        return "armor/hats/hats";
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
    public class Hat extends SimplePurchase<Hat> {
        private final CustomItem item;

        protected Hat(String id, Config config) {
            super(HatManager.this, id, config);
            this.item = config.getCustomItem("item", CustomItem.AIR);
        }
    }
}
