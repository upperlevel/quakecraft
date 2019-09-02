package xyz.upperlevel.quakecraft.shop.gun;

import lombok.Getter;
import org.bukkit.Color;
import xyz.upperlevel.quakecraft.QuakeAccount;
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
    public void setSelected(QuakeAccount player, Laser purchase) {
        player.setSelectedLaser(purchase);
    }

    @Override
    public Laser getSelected(QuakeAccount player) {
        return player.getSelectedLaser();
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
