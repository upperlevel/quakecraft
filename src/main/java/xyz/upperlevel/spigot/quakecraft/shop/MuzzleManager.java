package xyz.upperlevel.spigot.quakecraft.shop;

import lombok.Getter;
import xyz.upperlevel.spigot.quakecraft.core.particle.Particle;
import xyz.upperlevel.spigot.quakecraft.core.particle.ParticleType;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;
import xyz.upperlevel.uppercore.gui.config.itemstack.CustomItem;

import java.util.List;
import java.util.stream.Collectors;

public class MuzzleManager extends PurchaseManager<MuzzleManager.Muzzle> {

    @Override
    public Muzzle deserialize(String id, Config config) {
        try {
            return new Muzzle(id, config);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in muzzle \"" + id + "\"");
            throw e;
        }
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
    public String getPurchaseName() {
        return "muzzle";
    }


    @Getter
    public class Muzzle extends Purchase<Muzzle>{
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
