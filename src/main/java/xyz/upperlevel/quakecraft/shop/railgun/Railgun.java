package xyz.upperlevel.quakecraft.shop.railgun;

import lombok.AccessLevel;
import lombok.Getter;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.quakecraft.shop.gun.*;
import xyz.upperlevel.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Getter
public class Railgun {

    public static PlaceholderValue<String> CUSTOM_NAME;
    private final String id;
    private PlaceholderValue<String> name;
    @Getter(value = AccessLevel.NONE)
    private CaseManager.Case gcase;
    private LaserManager.Laser laser;
    private BarrelManager.Barrel barrel;
    private MuzzleManager.Muzzle muzzle;
    private TriggerManager.Trigger trigger;

    private PlaceholderValue<String> killMessage;

    private PlaceholderRegistry<?> placeholders;

    public Railgun(String id, PlaceholderValue<String> name, CaseManager.Case gcase, LaserManager.Laser laser, BarrelManager.Barrel barrel, MuzzleManager.Muzzle muzzle, TriggerManager.Trigger trigger, PlaceholderValue<String> killMessage) {
        this.id = id;
        this.name = name;
        this.gcase = gcase;
        this.laser = laser;
        this.barrel = barrel;
        this.muzzle = muzzle;
        this.trigger = trigger;
        this.killMessage = killMessage;

        this.placeholders = PlaceholderRegistry.create();
        processPlaceholders(placeholders);
    }

    public Railgun(String id, GunCategory cat, Config config) {
        this.id = id;
        this.name = config.getMessageStrRequired("name");

        String caseName = config.getStringRequired("case");
        String laserName = config.getStringRequired("laser");
        String barrelName = config.getStringRequired("barrel");
        String muzzleName = config.getStringRequired("muzzle");
        String triggerName = config.getStringRequired("trigger");

        this.gcase = cat.getCases().get(caseName);
        this.laser = cat.getLasers().get(laserName);
        this.barrel = cat.getBarrels().get(barrelName);
        this.muzzle = cat.getMuzzles().get(muzzleName);
        this.trigger = cat.getTriggers().get(triggerName);


        if (barrel == null) {
            throw config.invalidConfigException("barrel", "Cannot find barrel \"" + barrelName + "\"");
        }
        if (gcase == null) {
            throw config.invalidConfigException("case", "Cannot find case \"" + caseName + "\"");
        }
        if (laser == null) {
            throw config.invalidConfigException("laser", "Cannot find laser \"" + laserName + "\"");
        }
        if (muzzle == null) {
            throw config.invalidConfigException("muzzle", "Cannot find muzzle \"" + muzzleName + "\"");
        }
        if (trigger == null) {
            throw config.invalidConfigException("trigger", "Cannot find trigger \"" + triggerName + "\"");
        }

        this.killMessage = config.getMessageStr("message");
        this.placeholders = PlaceholderRegistry.create();
        processPlaceholders(placeholders);
    }

    public boolean canSelect(Profile profile) {
        Set<Purchase<?>> purchases = profile.getPurchases();
        for (Purchase<?> p : getComponents()) {
            if (p.getCost() > 0 && !purchases.contains(p))
                return false;
        }
        return true;
    }

    public List<? extends Purchase<?>> getComponents() {
        return Arrays.asList(
                gcase,
                laser,
                barrel,
                muzzle,
                trigger
        );
    }

    public void select(Profile profile) {
        profile.setRailgun(getComponents());
    }

    public boolean isSelected(Profile profile) {
        return profile.getSelectedCase() == gcase &&
                profile.getSelectedLaser() == laser &&
                profile.getSelectedBarrel() == barrel &&
                profile.getSelectedMuzzle() == muzzle &&
                profile.getSelectedTrigger() == trigger;
    }

    public CaseManager.Case getCase() {
        return gcase;
    }

    protected void processPlaceholders(PlaceholderRegistry<?> reg) {
        reg.set("case", p -> gcase.getName().resolve(p));
        reg.set("laser", p -> laser.getName().resolve(p));
        reg.set("barrel", p -> barrel.getName().resolve(p));
        reg.set("muzzle", p -> muzzle.getName().resolve(p));
        reg.set("trigger", p -> trigger.getName().resolve(p));
        reg.set("kill_message", p -> killMessage != null ? killMessage.resolve(p) : "none");
    }

    public static void loadConfig() {
        CUSTOM_NAME = Quake.get().getCustomConfig().getMessageStrRequired("game.custom-gun");
    }
}
