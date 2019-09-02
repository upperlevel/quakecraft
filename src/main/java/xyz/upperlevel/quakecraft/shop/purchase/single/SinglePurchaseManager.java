package xyz.upperlevel.quakecraft.shop.purchase.single;

import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseGui;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseManager;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigException;

import java.util.Map;

public abstract class SinglePurchaseManager<P extends SimplePurchase<P>> extends PurchaseManager<P> {
    private final String guiLoc;
    private final String configLoc;
    private final String registryLoc;


    public SinglePurchaseManager(PurchaseRegistry registry, String purchaseName, String guiLoc, String configLoc, String registryLoc) {
        super(registry, purchaseName);
        this.guiLoc = guiLoc;
        this.configLoc = configLoc;
        this.registryLoc = registryLoc;
    }

    public SinglePurchaseManager(PurchaseRegistry registry, String purchaseName, String loc) {
        this(registry, purchaseName, loc + ".gui", loc + ".types", loc);
    }

    public void loadConfig(Map<String, Config> config) {
        for (Map.Entry<String, Config> entry : config.entrySet()) {
            try {
                add(deserialize(entry.getKey(), entry.getValue()));
            } catch (InvalidConfigException e) {
                e.addLocation("in " + getPurchaseName() + " '" + entry.getKey() + "'");
                throw e;
            }
        }
        if (getDefault() == null) {
            throw new InvalidConfigException("No default value for " + getPurchaseName());
        }
    }

    public void loadConfig() {
        loadConfig(Quake.getConfigSection(
                "shop." + this.configLoc
        ).asConfigMap());
    }


    public void loadGui(String id, Config config) {
        PurchaseGui gui;
        try {
            gui = PurchaseGui.deserialize(id, config, this);
        } catch (InvalidConfigException e) {
            // TODO: remove?
            e.addLocation("in '" + getPurchaseName() + "' gui");
            throw e;
        }
        setGui(gui);
        Quake.get().getGuis().register(id, gui);
    }

    public void loadGui() {
        Config config = Quake.getConfigSection("shop." + guiLoc);
        loadGui(registryLoc, config);
    }

    public void load() {
        loadConfig();
        loadGui();
    }
}
