package xyz.upperlevel.quakecraft.shop.armor;

import lombok.Getter;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.shop.Category;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;

import java.util.logging.Logger;

@Getter
public class ArmorCategory extends Category {
    private HatManager hats;
    private ChestplateManager chestplates;
    private LeggingManager leggings;
    private BootManager boots;

    public ArmorCategory(PurchaseRegistry registry) {
        super(registry);

        hats = new HatManager(registry);
        chestplates = new ChestplateManager(registry);
        leggings = new LeggingManager(registry);
        boots = new BootManager(registry);
    }

    public void load() {
        final Logger logger = Quake.get().getLogger();
        logger.info("Init loading armor");

        loadGui();
        logger.info("Loaded Armor GUI");

        hats.load();
        logger.info("Loaded " + hats.getPurchases().size() + " hats");
        chestplates.load();
        logger.info("Loaded " + chestplates.getPurchases().size() + " chestplates");
        leggings.load();
        logger.info("Loaded " + leggings.getPurchases().size() + " leggings");
        boots.load();
        logger.info("Loaded " + boots.getPurchases().size() + " boots");

        logger.info("Shop loaded successfully!");
    }

    @Override
    public String getGuiLoc() {
        return "armor.gui";
    }

    @Override
    public String getGuiRegistryName() {
        return "armor.main";
    }
}
