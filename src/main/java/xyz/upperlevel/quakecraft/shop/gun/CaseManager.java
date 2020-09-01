package xyz.upperlevel.quakecraft.shop.gun;

import lombok.Getter;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.UItem;

public class CaseManager extends SinglePurchaseManager<CaseManager.Case> {

    public CaseManager(PurchaseRegistry registry) {
        super(registry, "case", "gun.cases");
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
    public void setSelected(Player player, Case purchase) {
        Quake.getProfileController().updateProfile(player.getUniqueId(), new Profile().setSelectedCase(purchase));
    }

    @Override
    public Case getSelected(Profile profile) {
        return profile.getSelectedCase();
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