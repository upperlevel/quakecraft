package xyz.upperlevel.quakecraft.shop.armor;

import lombok.Getter;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.UItem;

public class HatManager extends SinglePurchaseManager<HatManager.Hat> {

    public HatManager(PurchaseRegistry registry) {
        super(registry, "hat", "armor.hats");
    }

    @Override
    public Hat deserialize(String id, Config config) {
        return new Hat(id, config);
    }

    @Override
    public void setSelected(Profile profile, Hat purchase) {
        Quake.getProfileController().updateProfile(profile.getId(), new Profile().setSelectedHat(purchase));
    }

    @Override
    public Hat getSelected(Profile profile) {
        return profile.getSelectedHat();
    }

    @Getter
    public class Hat extends SimplePurchase<Hat> {
        private final UItem item;

        protected Hat(String id, Config config) {
            super(HatManager.this, id, config);
            this.item = config.getUItem("item", UItem.AIR);
        }
    }
}

