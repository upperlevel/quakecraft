package xyz.upperlevel.spigot.quakecraft.shop.gun;

import lombok.Getter;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.shop.Purchase;
import xyz.upperlevel.spigot.quakecraft.shop.PurchaseManager;
import xyz.upperlevel.spigot.quakecraft.shop.PurchaseRegistry;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.CustomItem;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

public class CaseManager extends PurchaseManager<CaseManager.Case> {

    public CaseManager(PurchaseRegistry registry) {
        super(registry);
        getGui().setEnchantSelected(false);
    }

    @Override
    public Case deserialize(String id, Config config) {
        return new Case(id, config);
    }

    @Override
    public String getGuiLoc() {
        return "cases";
    }

    @Override
    public String getConfigLoc() {
        return "gun/cases";
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
    public class Case extends Purchase<Case> {
        private final CustomItem item;

        protected Case(String id, Config config) {
            super(CaseManager.this, id, config);
            this.item = CustomItem.deserialize(config.getConfigRequired("item"));
        }
    }
}