package xyz.upperlevel.spigot.quakecraft.shop;

import lombok.Getter;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.shop.armor.ArmorCategory;
import xyz.upperlevel.spigot.quakecraft.shop.gun.GunCategory;

import java.util.logging.Logger;

@Getter
public class ShopCategory extends Category {

    private GunCategory guns;
    private ArmorCategory armors;
    private KillSoundManager killSounds;

    public ShopCategory() {
        super(new PurchaseRegistry());

        guns = new GunCategory(registry);
        armors = new ArmorCategory(registry);
        killSounds = new KillSoundManager(registry);
    }

    public void load() {
        final Logger logger = QuakeCraftReloaded.get().getLogger();
        logger.info("Init loading shop");

        //Load gui
        loadGui();
        logger.info("Loaded Shop GUI");

        guns.load();
        armors.load();
        killSounds.load();

        logger.info("Shop loaded successfully");
    }

    @Override
    public String getGuiLoc() {
        return "shop";
    }
}
