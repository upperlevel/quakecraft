package xyz.upperlevel.spigot.quakecraft.shop.gun;

import lombok.Getter;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.core.particle.Particle;
import xyz.upperlevel.spigot.quakecraft.shop.Purchase;
import xyz.upperlevel.spigot.quakecraft.shop.PurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.gui.config.itemstack.CustomItem;

import java.util.List;
import java.util.stream.Collectors;

public class MuzzleManager extends PurchaseManager<MuzzleManager.Muzzle> {

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

        public Muzzle(String id, String name, float cost, CustomItem icon, boolean def, List<Particle> particles) {
            super(MuzzleManager.this, id, name, cost, icon, def);
            this.particles = particles;
        }

        protected Muzzle(String id, Config config) {
            super(MuzzleManager.this, id, config);
            this.particles = config.getConfigListRequired("particles")
                    .stream()
                    .map(Particle::deserialize)
                    .collect(Collectors.toList());
        }
    }
}
