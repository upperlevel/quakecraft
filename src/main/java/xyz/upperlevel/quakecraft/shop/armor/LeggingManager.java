package xyz.upperlevel.quakecraft.shop.armor;

import lombok.Getter;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.UItem;

public class LeggingManager extends SinglePurchaseManager<LeggingManager.Legging> {

    public LeggingManager(PurchaseRegistry registry) {
        super(registry, "legging", "armor.leggings");
    }

    @Override
    public Legging deserialize(String id, Config config) {
        return new Legging(id, config);
    }

    @Override
    public void setSelected(Profile profile, Legging purchase) {
        Quake.getProfileController().updateProfile(profile.getId(), new Profile().setSelectedLeggings(purchase));
    }

    @Override
    public Legging getSelected(Profile profile) {
        return profile.getSelectedLeggings();
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

