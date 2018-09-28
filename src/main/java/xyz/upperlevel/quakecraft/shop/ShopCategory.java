package xyz.upperlevel.quakecraft.shop;

import lombok.Getter;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.shop.armor.ArmorCategory;
import xyz.upperlevel.quakecraft.shop.dash.DashCategory;
import xyz.upperlevel.quakecraft.shop.gun.GunCategory;

import java.util.logging.Logger;

@Getter
public class ShopCategory extends Category {
    private GunCategory guns;
    private ArmorCategory armors;
    private KillSoundManager killSounds;

    private DashCategory dashes;

    public ShopCategory() {
        super(new PurchaseRegistry());

        guns = new GunCategory(registry);
        armors = new ArmorCategory(registry);
        killSounds = new KillSoundManager(registry);
        dashes = new DashCategory(registry);
    }

    public void load() {
        final Logger logger = Quakecraft.get().getLogger();
        logger.info("Init loading shop");

        //Load gui
        loadGui();
        logger.info("Loaded Shop GUI");

        guns.load();
        armors.load();
        killSounds.load();
        dashes.load();

        logger.info("Shop loaded successfully");
    }

    @Override
    public String getGuiLoc() {
        return "shop_gui";
    }

    @Override
    public String getGuiRegistryName() {
        return "shop";
    }
}
