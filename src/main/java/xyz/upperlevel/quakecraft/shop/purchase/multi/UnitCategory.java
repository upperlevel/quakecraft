package xyz.upperlevel.quakecraft.shop.purchase.multi;

import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.shop.Category;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseGui;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.uppercore.config.Config;

import java.io.File;
import java.util.List;


public abstract class UnitCategory extends Category {
    public UnitCategory(PurchaseRegistry registry) {
        super(registry);
    }

    @Override
    protected void loadGui(String name, File file) {
        loadGui(name, Config.fromYaml(file));
    }

    private void loadGui(String id, Config config) {
        PurchaseGui gui = new PurchaseGui(config);
        this.gui = gui;
        for(MultiPurchaseManager manager : getChildren()) {
            gui.addPurchase(
                    manager,
                    config.get(manager.getPartName() + "-slots", int[].class, null)
            );
        }

        Quake.get().getGuis().register(id, gui);
    }

    public void loadConfig() {
        loadConfig(Config.fromYaml(new File(
                Quake.get().getDataFolder(),
                "shop/" + getConfigLoc() + ".yml"
        )));
    }

    private void loadConfig(Config config) {
        for (MultiPurchaseManager child : getChildren()) {
            child.loadConfig(config.getConfig(child.getPartName()).asConfigMap());
        }
    }

    protected abstract List<MultiPurchaseManager> getChildren();

    public abstract String getConfigLoc();
}
