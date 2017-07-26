package xyz.upperlevel.spigot.quakecraft.shop.dash;

import org.bukkit.DyeColor;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.multi.MultiPurchaseManager;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.multi.UnitCategory;
import xyz.upperlevel.uppercore.gui.GuiUtil;
import xyz.upperlevel.uppercore.itemstack.CustomItem;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class DashCategory extends UnitCategory {
    public static final CustomItem GOT = new CustomItem(GuiUtil.stainedGlassPane(DyeColor.GREEN, ""));
    public static final CustomItem MISSING = new CustomItem(GuiUtil.stainedGlassPane(DyeColor.RED, ""));

    private DashPowerManager power;
    private DashCooldownManager cooldown;

    public DashCategory(PurchaseRegistry registry) {
        super(registry);

        power = new DashPowerManager(registry);
        cooldown = new DashCooldownManager(registry);
    }

    @Override
    protected List<MultiPurchaseManager> getChildren() {
        return Arrays.asList(
                power,
                cooldown
        );
    }

    @Override
    public String getConfigLoc() {
        return "dash-upgrades";
    }

    @Override
    public String getGuiLoc() {
        return "dash-upgrades";
    }

    public void load() {
        final Logger logger = QuakeCraftReloaded.get().getLogger();
        logger.info("Init loading dash");

        loadGui();
        logger.info("Loaded Dash GUI");
        loadGui();
        logger.info("Loaded Dash config");

        logger.info("Dash loaded successfully!");
    }
}
