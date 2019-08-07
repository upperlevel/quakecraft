package xyz.upperlevel.quakecraft.shop.gun;

import lombok.Getter;
import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.UItem;

public class CaseManager extends SinglePurchaseManager<CaseManager.Case> {

    public CaseManager(PurchaseRegistry registry) {
        super(registry, "gun.cases");
    }

    @Override
    public void load() {
        super.load();
        getGui().setEnchantSelected(false);
    }

    @Override
    public Case deserialize(String id, Config config) {
        return new Case(id, config);
    }

    @Override
    public void setSelected(QuakeAccount player, Case purchase) {
        player.setSelectedCase(purchase);
    }

    @Override
    public Case getSelected(QuakeAccount player) {
        return player.getSelectedCase();
    }

    @Override
    public String getPurchaseName() {
        return "case";
    }


    @Getter
    public class Case extends SimplePurchase<Case> {
        private final UItem item;

        protected Case(String id, Config config) {
            super(CaseManager.this, id, config);
            this.item = config.getUItemRequired("item");
        }
    }
}