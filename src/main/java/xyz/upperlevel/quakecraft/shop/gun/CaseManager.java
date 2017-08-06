package xyz.upperlevel.quakecraft.shop.gun;

import lombok.Getter;
import xyz.upperlevel.quakecraft.QuakePlayer;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.CustomItem;

public class CaseManager extends SinglePurchaseManager<CaseManager.Case> {

    public CaseManager(PurchaseRegistry registry) {
        super(registry);
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
    public String getGuiLoc() {
        return "gun/cases/cases_gui";
    }

    @Override
    public String getConfigLoc() {
        return "gun/cases/cases";
    }

    @Override
    public void setSelected(QuakePlayer player, Case purchase) {
        player.setSelectedCase(purchase);
    }

    @Override
    public Case getSelected(QuakePlayer player) {
        return player.getSelectedCase();
    }

    @Override
    public String getPurchaseName() {
        return "case";
    }


    @Getter
    public class Case extends SimplePurchase<Case> {
        private final CustomItem item;

        protected Case(String id, Config config) {
            super(CaseManager.this, id, config);
            this.item = CustomItem.deserialize(config.getConfigRequired("item"));
        }
    }
}