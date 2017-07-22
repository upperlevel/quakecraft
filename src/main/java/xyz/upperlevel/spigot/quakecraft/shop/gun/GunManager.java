package xyz.upperlevel.spigot.quakecraft.shop.gun;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.core.particle.Particle;
import xyz.upperlevel.spigot.quakecraft.shop.Purchase;
import xyz.upperlevel.spigot.quakecraft.shop.PurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;
import xyz.upperlevel.uppercore.gui.config.itemstack.CustomItem;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GunManager extends PurchaseManager<GunManager.Gun> {

    private final GunCategory parent;

    @Override
    public Gun deserialize(String id, Config config) {
        return new Gun(id, config);
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
    public void setSelected(QuakePlayer player, Gun purchase) {
        player.setSelectedGun(purchase);
    }

    @Override
    public Gun getSelected(QuakePlayer player) {
        return player.getSelectedGun();
    }

    @Override
    public String getPurchaseName() {
        return "gun";
    }


    @Getter
    public class Gun extends Purchase<Gun> {
        private final BarrelManager.Barrel barrel;
        private final CaseManager.Case gcase;
        private final LaserManager.Laser laser;
        private final MuzzleManager.Muzzle muzzle;
        private final TriggerManager.Trigger trigger;

        private final List<CustomItem> links;
        private final List<Particle> particles;

        public Gun(String id, PlaceholderValue<String> name, float cost, CustomItem icon, boolean def,
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

            this.barrel = parent.getBarrels().get(barrelName);
            this.gcase = parent.getCases().get(caseName);
            this.laser = parent.getLasers().get(laserName);
            this.muzzle = parent.getMuzzles().get(muzzleName);
            this.trigger = parent.getTriggers().get(triggerName);

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
