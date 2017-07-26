package xyz.upperlevel.spigot.quakecraft.shop.gun;

import lombok.Getter;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.shop.Category;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.PurchaseRegistry;

import java.util.logging.Logger;

@Getter
public class GunCategory extends Category{
    private BarrelManager barrels;
    private CaseManager cases;
    private LaserManager lasers;
    private MuzzleManager muzzles;
    private TriggerManager triggers;

    private GunManager guns;

    public GunCategory(PurchaseRegistry registry) {
        super(registry);

        barrels = new BarrelManager(registry);
        cases = new CaseManager(registry);
        lasers = new LaserManager(registry);
        muzzles = new MuzzleManager(registry);
        triggers = new TriggerManager(registry);
        guns = new GunManager(registry, this);
    }

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
        return "gun";
    }
}
