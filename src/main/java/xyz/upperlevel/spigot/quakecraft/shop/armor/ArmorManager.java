package xyz.upperlevel.spigot.quakecraft.shop.armor;

import lombok.Getter;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.uppercore.gui.Gui;

import java.io.File;
import java.util.logging.Logger;

@Getter
public class ArmorManager {
    private HatManager hats = new HatManager();
    private ChestplateManager kits = new ChestplateManager();
    private LeggingManager leggings = new LeggingManager();
    private BootManager boots = new BootManager();

    private Gui gui;

    public void load() {
        final Logger logger = QuakeCraftReloaded.get().getLogger();
        logger.info("Init loading armor");

        {//Load gui
            File guiFile = new File(
                    QuakeCraftReloaded.get().getDataFolder(),
                    "guis" + File.separator + "armor.yml"
                    );
            gui = QuakeCraftReloaded.get().getGuis().load(guiFile);
            logger.info("Loaded Armor GUI");
        }

        hats.load();
        logger.info("Loaded " + hats.getPurchases().size() + " hats");
        kits.load();
        logger.info("Loaded " + kits.getPurchases().size() + " kits");
        leggings.load();
        logger.info("Loaded " + leggings.getPurchases().size() + " leggings");
        boots.load();
        logger.info("Loaded " + boots.getPurchases().size() + " boots");

        logger.info("Shop loaded successfully!");
    }
}
