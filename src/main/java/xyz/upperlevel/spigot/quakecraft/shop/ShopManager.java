package xyz.upperlevel.spigot.quakecraft.shop;

import lombok.Getter;

@Getter
public class ShopManager {
    private BarrelManager barrels = new BarrelManager();
    private CaseManager cases = new CaseManager();
    private LaserManager lasers = new LaserManager();
    private MuzzleManager muzzles = new MuzzleManager();
    private TriggerManager triggers = new TriggerManager();

    private GunManager guns = new GunManager(this);

    public void load() {
        barrels.load();
        cases.load();
        lasers.load();
        muzzles.load();
        triggers.load();

        guns.load();
    }

}
