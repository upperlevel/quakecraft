package xyz.upperlevel.quakecraft.shop.armor;

import lombok.Getter;
import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.UItem;

public class LeggingManager extends SinglePurchaseManager<LeggingManager.Legging> {

    public LeggingManager(PurchaseRegistry registry) {
        super(registry, "armor.leggings");
    }

    @Override
    public Legging deserialize(String id, Config config) {
        return new Legging(id, config);
    }

    @Override
    public void setSelected(QuakeAccount player, Legging purchase) {
        player.setSelectedLegging(purchase);
    }

    @Override
    public Legging getSelected(QuakeAccount player) {
        return player.getSelectedLegging();
    }

    @Override
    public String getPurchaseName() {
        return "legging";
    }

    @Getter
    public class Legging extends SimplePurchase<Legging> {
        private final UItem item;

        protected Legging(String id, Config config) {
            super(LeggingManager.this, id, config);
            this.item = config.getUItem("item", UItem.AIR);

        }
    }
}

