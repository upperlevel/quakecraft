package xyz.upperlevel.spigot.quakecraft.shop.railgun;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.ChatColor;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.shop.gun.*;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;
import xyz.upperlevel.uppercore.util.TextUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Getter
public class Railgun {
    public static PlaceholderValue<String> CUSTOM_NAME ;
    private final String id;
    private PlaceholderValue<String> name;
    @Getter(value = AccessLevel.NONE)
    private CaseManager.Case gcase;
    private LaserManager.Laser laser;
    private BarrelManager.Barrel barrel;
    private MuzzleManager.Muzzle muzzle;
    private TriggerManager.Trigger trigger;

    private String killMessage;

    private PlaceholderRegistry placeholders;

    public Railgun(String id, PlaceholderValue<String> name, CaseManager.Case gcase, LaserManager.Laser laser, BarrelManager.Barrel barrel, MuzzleManager.Muzzle muzzle, TriggerManager.Trigger trigger, String killMessage) {
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

        String rawMessage = config.getString("message");
        this.killMessage = rawMessage == null ? null : TextUtil.translatePlain(rawMessage);
        this.placeholders = PlaceholderRegistry.create();
        processPlaceholders(placeholders);
    }

    public boolean canSelect(QuakePlayer player) {
        return player.getPurchases().containsAll(getComponents());
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

    public void select(QuakePlayer player) {
        player.setGunComponents(getComponents());
    }

    public boolean isSelected(QuakePlayer player) {
        return  player.getSelectedCase() == gcase &&
                player.getSelectedLaser() == laser &&
                player.getSelectedBarrel() == barrel &&
                player.getSelectedMuzzle() == muzzle &&
                player.getSelectedTrigger() == trigger;
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
        reg.set("kill-message", p -> killMessage != null ? killMessage : "none");
    }

    public static void loadConfig() {
        CUSTOM_NAME = QuakeCraftReloaded.get().getCustomConfig().getMessageStr("game.custom-gun");
    }
}
