package xyz.upperlevel.spigot.quakecraft.shop.gun;

import lombok.Getter;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.core.particle.Particle;
import xyz.upperlevel.spigot.quakecraft.shop.Purchase;
import xyz.upperlevel.spigot.quakecraft.shop.PurchaseManager;
import xyz.upperlevel.spigot.quakecraft.shop.PurchaseRegistry;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.CustomItem;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.util.List;
import java.util.stream.Collectors;

public class MuzzleManager extends PurchaseManager<MuzzleManager.Muzzle> {

    public MuzzleManager(PurchaseRegistry registry) {
        super(registry);
    }

    @Override
    public Muzzle deserialize(String id, Config config) {
        return new Muzzle(id, config);
    }

    @Override
    public String getGuiLoc() {
        return "muzzles";
    }

    @Override
    public String getConfigLoc() {
        return "gun/muzzles";
    }

    @Override
    public void setSelected(QuakePlayer player, Muzzle purchase) {
        player.setSelectedMuzzle(purchase);
    }

    @Override
    public Muzzle getSelected(QuakePlayer player) {
        return player.getSelectedMuzzle();
    }

    @Override
    public String getPurchaseName() {
        return "muzzle";
    }


    @Getter
    public class Muzzle extends Purchase<Muzzle> {
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
