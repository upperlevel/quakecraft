package xyz.upperlevel.quakecraft.shop.gun;

import lombok.Getter;
import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.particle.Particle;
import xyz.upperlevel.uppercore.config.Config;

import java.util.List;
import java.util.stream.Collectors;

public class MuzzleManager extends SinglePurchaseManager<MuzzleManager.Muzzle> {

    public MuzzleManager(PurchaseRegistry registry) {
        super(registry);
    }

    @Override
    public Muzzle deserialize(String id, Config config) {
        return new Muzzle(id, config);
    }

    @Override
    public String getGuiLoc() {
        return "gun/muzzles/muzzles_gui";
    }

    @Override
    public String getConfigLoc() {
        return "gun/muzzles/muzzles";
    }

    @Override
    public void setSelected(QuakeAccount player, Muzzle purchase) {
        player.setSelectedMuzzle(purchase);
    }

    @Override
    public Muzzle getSelected(QuakeAccount player) {
        return player.getSelectedMuzzle();
    }

    @Override
    public String getPurchaseName() {
        return "muzzle";
    }


    @Getter
    public class Muzzle extends SimplePurchase<Muzzle> {
        private final List<Particle> particles;


        protected Muzzle(String id, Config config) {
            super(MuzzleManager.this, id, config);
            this.particles = config.getConfigListRequired("particles")
                    .stream()
                    .map(Particle::deserialize)
                    .collect(Collectors.toList());
        }
    }
}
