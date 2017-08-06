package xyz.upperlevel.spigot.quakecraft.shop.purchase.multi;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.shop.Category;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.PurchaseGui;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.ConfigUtils;
import xyz.upperlevel.uppercore.gui.GuiId;

import java.io.File;
import java.util.List;


public abstract class UnitCategory extends Category {
    public UnitCategory(PurchaseRegistry registry) {
        super(registry);
    }

    @Override
    protected void loadGui(File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        loadGui(QuakeCraftReloaded.get(), file.getName().replaceFirst("[.][^.]+$", ""), Config.wrap(config));
    }

    private void loadGui(QuakeCraftReloaded plugin, String id, Config config) {
        PurchaseGui gui = new PurchaseGui(plugin, config);
        this.gui = gui;
        for(MultiPurchaseManager manager : getChildren()) {
            gui.add(
                    manager,
                    PurchaseGui.deserializeSlots(config.getCollectionRequired(manager.getPartName() + "-slots"))
            );
        }

        QuakeCraftReloaded.get().getGuis().register(new GuiId(plugin, id, gui));
    }

    public void loadConfig() {
        loadConfig(Config.wrap(ConfigUtils.loadConfig(
                QuakeCraftReloaded.get(),
                "shop" + File.separator + getConfigLoc() + ".yml"
        )));
    }

    private void loadConfig(Config config) {
        for (MultiPurchaseManager child : getChildren()) {
            child.loadConfig(ConfigUtils.loadConfigMap(
                    config.getSectionRequired(child.getPartName()),
                    QuakeCraftReloaded.get(),
                    child.getPurchaseName()
            ));
        }
    }

    protected abstract List<MultiPurchaseManager> getChildren();

    public abstract String getConfigLoc();
}
