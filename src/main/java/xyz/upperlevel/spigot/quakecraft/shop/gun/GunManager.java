package xyz.upperlevel.spigot.quakecraft.shop.gun;

import lombok.Getter;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.core.particle.Particle;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;
import xyz.upperlevel.uppercore.itemstack.CustomItem;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;

import java.util.List;
import java.util.stream.Collectors;

public class GunManager extends SinglePurchaseManager<GunManager.Gun> {

    private final GunCategory parent;

    public GunManager(PurchaseRegistry registry, GunCategory parent) {
        super(registry);
        this.parent = parent;
    }

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
    public class Gun extends SimplePurchase<Gun> {
        private final BarrelManager.Barrel barrel;
        private final CaseManager.Case gcase;
        private final LaserManager.Laser laser;
        private final MuzzleManager.Muzzle muzzle;
        private final TriggerManager.Trigger trigger;

        private final List<CustomItem> links;
        private final List<Particle> particles;

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

        @Override
        public void fillPlaceholderSession(PlaceholderRegistry session) {
            super.fillPlaceholderSession(session);
            session.set("barrel", () -> getBarrel().getId());
            session.set("case", () -> getGcase().getId());
            session.set("laser", () -> getLaser().getId());
            session.set("muzzle", () -> getMuzzle().getId());
            session.set("trigger", () -> getTrigger().getId());
        }
    }
}
