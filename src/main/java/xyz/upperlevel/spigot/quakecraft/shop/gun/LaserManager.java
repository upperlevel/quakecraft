package xyz.upperlevel.spigot.quakecraft.shop.gun;

import lombok.Getter;
import org.bukkit.Color;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;

public class LaserManager extends SinglePurchaseManager<LaserManager.Laser> {

    public LaserManager(PurchaseRegistry registry) {
        super(registry);
    }

    @Override
    public Laser deserialize(String id, Config config) {
        return new Laser(id, config);
    }

    @Override
    public String getGuiLoc() {
        return "lasers";
    }

    @Override
    public String getConfigLoc() {
        return "gun/lasers";
    }

    @Override
    public void setSelected(QuakePlayer player, Laser purchase) {
        player.setSelectedLaser(purchase);
    }

    @Override
    public Laser getSelected(QuakePlayer player) {
        return player.getSelectedLaser();
    }

    @Override
    public String getPurchaseName() {
        return "laser";
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
