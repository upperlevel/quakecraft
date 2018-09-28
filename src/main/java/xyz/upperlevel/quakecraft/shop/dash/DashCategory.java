package xyz.upperlevel.quakecraft.shop.dash;

import lombok.Getter;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.multi.MultiPurchaseManager;
import xyz.upperlevel.quakecraft.shop.purchase.multi.UnitCategory;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Getter
public class DashCategory extends UnitCategory {
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
        return "dash/dash_upgrades";
    }

    @Override
    public String getGuiLoc() {
        return "dash/dash_upgrades_gui";
    }

    @Override
    public String getGuiRegistryName() {
        return "dash_upgrades";
    }

    public void load() {
        final Logger logger = Quakecraft.get().getLogger();
        logger.info("Init loading dash");

        loadGui();
        logger.info("Loaded Dash GUI");
        loadConfig();
        logger.info("Loaded Dash config");

        logger.info("Dash loaded successfully!");
    }
}
