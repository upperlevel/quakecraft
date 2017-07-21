package xyz.upperlevel.spigot.quakecraft.shop.gun;

import lombok.Getter;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.shop.Category;

import java.util.logging.Logger;

@Getter
public class GunCategory extends Category{
    private BarrelManager barrels = new BarrelManager();
    private CaseManager cases = new CaseManager();
    private LaserManager lasers = new LaserManager();
    private MuzzleManager muzzles = new MuzzleManager();
    private TriggerManager triggers = new TriggerManager();

    private GunManager guns = new GunManager(this);

    public void load() {
        final Logger logger = QuakeCraftReloaded.get().getLogger();
        logger.info("Init loading guns");

        loadGui();
        logger.info("Loaded Gun GUI");

        barrels.load();
        logger.info("Loaded " + barrels.getPurchases().size() + " barrels");
        cases.load();
        logger.info("Loaded " + cases.getPurchases().size() + " cases");
        lasers.load();
        logger.info("Loaded " + lasers.getPurchases().size() + " lasers");
        muzzles.load();
        logger.info("Loaded " + muzzles.getPurchases().size() + " muzzles");
        triggers.load();
        logger.info("Loaded " + triggers.getPurchases().size() + " triggers");

        guns.load();
        logger.info("Loaded " + guns.getPurchases().size() + " guns");

        logger.info("Guns loaded successfully!");
    }

    @Override
    public String getGuiLoc() {
        return "guns";
    }
}
