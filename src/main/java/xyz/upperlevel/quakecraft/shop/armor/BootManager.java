package xyz.upperlevel.quakecraft.shop.armor;

import lombok.Getter;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.UItem;

public class BootManager extends SinglePurchaseManager<BootManager.Boot> {
    public BootManager(PurchaseRegistry registry) {
        super(registry, "boot", "armor.boots");
    }

    @Override
    public Boot deserialize(String id, Config config) {
        return new Boot(id, config);
    }

    @Override
    public void setSelected(Profile profile, Boot purchase) {
        Quake.getProfileController().updateProfile(profile.getId(), new Profile().setSelectedBoots(purchase));
    }

    @Override
    public Boot getSelected(Profile profile) {
        return profile.getSelectedBoots();
    }

    @Getter
    public class Boot extends SimplePurchase<Boot> {
        private final UItem item;

        protected Boot(String id, Config config) {
            super(BootManager.this, id, config);
            this.item = config.getUItem("item", UItem.AIR);
        }
    }
}
