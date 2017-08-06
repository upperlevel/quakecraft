package xyz.upperlevel.quakecraft.shop.purchase.single;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseGui;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseManager;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.ConfigUtils;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;
import xyz.upperlevel.uppercore.gui.GuiId;

import java.io.File;
import java.util.Map;

public abstract class SinglePurchaseManager<P extends SimplePurchase<P>> extends PurchaseManager<P> {
    public SinglePurchaseManager(PurchaseRegistry registry) {
        super(registry);
    }

    public abstract String getGuiLoc();

    public abstract String getConfigLoc();

    public void loadConfig(Map<String, Config> config) {
        for(Map.Entry<String, Config> entry : config.entrySet()) {
            try {
                add(deserialize(entry.getKey(), entry.getValue()));
            } catch (InvalidConfigurationException e) {
                e.addLocalizer("in " + getPurchaseName() + " '" + entry.getKey() + "'");
                throw e;
            }
        }
        if(getDefault() == null)
            throw new InvalidConfigurationException("No default value for " + getPurchaseName());
    }

    public void loadConfig() {
        loadConfig(ConfigUtils.loadConfigMap(
                Quakecraft.get(),
                "shop" + File.separator + getConfigLoc() + ".yml",
                getPurchaseName()
        ));
    }


    public void loadGui(Plugin plugin, String id, Config config) {
        PurchaseGui gui;
        try {
            gui = PurchaseGui.deserialize(Quakecraft.get(), id, config, this);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in '" + getPurchaseName() + "' gui");
            throw e;
        }
        setGui(gui);
        Quakecraft.get().getGuis().register(new GuiId(plugin, id, gui));
    }

    public void loadGui(Plugin plugin) {
        String guiLoc = getGuiLoc();
        if(guiLoc == null)
            return;
        FileConfiguration config = ConfigUtils.loadConfig(
                Quakecraft.get(),
                "shop" + File.separator + guiLoc + ".yml"
        );
        loadGui(plugin, guiLoc.replaceFirst(".+[/\\\\]", ""), Config.wrap(config));
    }

    public void load() {
        loadConfig();
        loadGui(Quakecraft.get());
    }
}
