package xyz.upperlevel.quakecraft.shop.armor;

import lombok.Getter;
import xyz.upperlevel.quakecraft.QuakePlayer;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.CustomItem;

public class LeggingManager extends SinglePurchaseManager<LeggingManager.Legging> {

    public LeggingManager(PurchaseRegistry registry) {
        super(registry);
    }

    @Override
    public Legging deserialize(String id, Config config) {
        return new Legging(id, config);
    }

    @Override
    public String getGuiLoc() {
        return "armor/leggings/leggings_gui";
    }

    @Override
    public String getConfigLoc() {
        return "armor/leggings/leggings";
    }

    @Override
    public void setSelected(QuakePlayer player, Legging purchase) {
        player.setSelectedLegging(purchase);
    }

    @Override
    public Legging getSelected(QuakePlayer player) {
        return player.getSelectedLegging();
    }

    @Override
    public String getPurchaseName() {
        return "legging";
    }

    @Getter
    public class Legging extends SimplePurchase<Legging> {
        private final CustomItem item;

        protected Legging(String id, Config config) {
            super(LeggingManager.this, id, config);
            this.item = config.getCustomItem("item", CustomItem.AIR);

        }
    }
}
