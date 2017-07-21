package xyz.upperlevel.spigot.quakecraft.shop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.upperlevel.spigot.quakecraft.core.particle.Particle;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;
import xyz.upperlevel.uppercore.gui.config.itemstack.CustomItem;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GunManager extends PurchaseManager<GunManager.Gun> {

    private final ShopManager shop;

    @Override
    public Gun deserialize(String id, Config config) {
        try {
            return new Gun(id, config);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in gun \"" + id + "\"");
            throw e;
        }
    }

    @Override
    public String getGuiLoc() {
        return "guns";
    }

    @Override
    public String getConfigLoc() {
        return "gun/guns";
    }

    @Override
    public String getPurchaseName() {
        return "gun";
    }


    @Getter
    public class Gun extends Purchase<Gun>{
        private final BarrelManager.Barrel barrel;
        private final CaseManager.Case gcase;
        private final LaserManager.Laser laser;
        private final MuzzleManager.Muzzle muzzle;
        private final TriggerManager.Trigger trigger;

        private final List<CustomItem> links;
        private final List<Particle> particles;

        public Gun(String id, String name, float cost, CustomItem icon, boolean def,
                   BarrelManager.Barrel barrel, CaseManager.Case gcase, LaserManager.Laser laser,
                   MuzzleManager.Muzzle muzzle, TriggerManager.Trigger trigger,
                   List<CustomItem> links, List<Particle> particles) {
            super(GunManager.this, id, name, cost, icon, def);
            this.barrel = barrel;
            this.gcase = gcase;
            this.laser = laser;
            this.muzzle = muzzle;
            this.trigger = trigger;
            this.links = links;
            this.particles = particles;
        }

        protected Gun(String id, Config config) {
            super(GunManager.this, id, config);

            String barrelName = config.getStringRequired("barrel");
            String caseName = config.getStringRequired("case");
            String laserName = config.getStringRequired("laser");
            String muzzleName = config.getStringRequired("muzzle");
            String triggerName = config.getStringRequired("trigger");

            this.barrel = shop.getBarrels().get(barrelName);
            this.gcase = shop.getCases().get(caseName);
            this.laser = shop.getLasers().get(laserName);
            this.muzzle = shop.getMuzzles().get(muzzleName);
            this.trigger = shop.getTriggers().get(triggerName);

            if(barrel == null)
                throw new InvalidConfigurationException("Cannot find barrel \"" + barrelName + "\"");
            if(gcase == null)
                throw new InvalidConfigurationException("Cannot find case \"" + caseName + "\"");
            if(laser == null)
                throw new InvalidConfigurationException("Cannot find laser \"" + laserName + "\"");
            if(muzzle == null)
                throw new InvalidConfigurationException("Cannot find muzzle \"" + muzzleName + "\"");
            if(trigger == null)
                throw new InvalidConfigurationException("Cannot find trigger \"" + triggerName + "\"");

            this.links = config.getConfigListRequired("links")
                    .stream()
                    .map(CustomItem::deserialize)
                    .collect(Collectors.toList());

            this.particles = config.getConfigListRequired("particles")
                    .stream()
                    .map(Particle::deserialize)
                    .collect(Collectors.toList());
        }
    }
}
