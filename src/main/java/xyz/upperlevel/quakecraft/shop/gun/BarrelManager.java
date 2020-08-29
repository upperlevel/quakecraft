package xyz.upperlevel.quakecraft.shop.gun;

import lombok.Getter;
import org.bukkit.FireworkEffect;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;

public class BarrelManager extends SinglePurchaseManager<BarrelManager.Barrel> {

    public BarrelManager(PurchaseRegistry registry) {
        super(registry, "barrel", "gun.barrels");
    }

    @Override
    public Barrel deserialize(String id, Config config) {
        return new Barrel(id, config);
    }

    @Override
    public void setSelected(Profile profile, Barrel purchase) {
        Quake.getProfileController().updateProfile(profile.getId(), new Profile().setSelectedBarrel(purchase));
    }

    @Override
    public Barrel getSelected(Profile profile) {
        return profile.getSelectedBarrel();
    }


    @Getter
    public class Barrel extends SimplePurchase<Barrel> {
        private final FireworkEffect.Type fireworkType;

        protected Barrel(String id, Config config) {
            super(BarrelManager.this, id, config);
            this.fireworkType = config.getRequired("firework-type", FireworkEffect.Type.class);
        }
    }
}
