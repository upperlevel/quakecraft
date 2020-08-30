package xyz.upperlevel.quakecraft.shop.gun;

import lombok.Getter;
import org.bukkit.Color;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.uppercore.config.Config;

public class LaserManager extends SinglePurchaseManager<LaserManager.Laser> {
    public LaserManager(PurchaseRegistry registry) {
        super(registry, "laser", "gun.lasers");
    }

    @Override
    public Laser deserialize(String id, Config config) {
        return new Laser(id, config);
    }

    @Override
    public void setSelected(Profile profile, Laser purchase) {
        profile.setSelectedLaser(purchase);
    }

    @Override
    public Laser getSelected(Profile profile) {
        return profile.getSelectedLaser();
    }

    @Getter
    public class Laser extends SimplePurchase<Laser> {
        private final Color fireworkColor;

        protected Laser(String id, Config config) {
            super(LaserManager.this, id, config);
            this.fireworkColor = config.getColorRequired("firework-color");
        }
    }
}
