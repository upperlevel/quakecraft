package xyz.upperlevel.spigot.quakecraft.shop;

import lombok.Getter;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.shop.armor.ArmorCategory;
import xyz.upperlevel.spigot.quakecraft.shop.gun.GunCategory;

import java.util.logging.Logger;

@Getter
public class ShopCategory extends Category{
    private GunCategory guns = new GunCategory();
    private ArmorCategory armors = new ArmorCategory();
    private KillSoundManager killSounds = new KillSoundManager();

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
