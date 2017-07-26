package xyz.upperlevel.spigot.quakecraft.shop.purchase.single;

import com.google.common.collect.Maps;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.PurchaseGui;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.PurchaseManager;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;
import xyz.upperlevel.uppercore.gui.GuiId;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static xyz.upperlevel.spigot.quakecraft.core.CollectionUtil.toMap;

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
    }

    public void loadConfig(File file) {
        if(!file.exists())
            throw new InvalidParameterException("Cannot find file " + file);

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        loadConfig((Map<String, Config>)config.getValues(false)
                .entrySet()
                .stream()
                .map(e -> {
                    Object o = e.getValue();
                    if(o instanceof Map)
                        return Maps.immutableEntry(e.getKey(), Config.wrap((Map)o));
                    else if(o instanceof ConfigurationSection)
                        return Maps.immutableEntry(e.getKey(), Config.wrap((ConfigurationSection) o));
                    else {
                        QuakeCraftReloaded.get().getLogger().severe("Cannot parse " + getPurchaseName() + e.getKey() + ": expected map (found: " + o.getClass().getSimpleName() + ")");
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(toMap(LinkedHashMap::new)));
    }

    public void loadConfig() {
        File file = new File(
                QuakeCraftReloaded.get().getDataFolder(),
                "shop" + File.separator + getConfigLoc() + ".yml"
        );
        loadConfig(file);
    }


    public void loadGui(Plugin plugin, String id, Config config) {
        PurchaseGui gui = PurchaseGui.deserialize(QuakeCraftReloaded.get(), id, config, this);
        setGui(gui);
        QuakeCraftReloaded.get().getGuis().register(new GuiId(plugin, id, gui));
    }

    public void loadGui(Plugin plugin) {
        String guiLoc = getGuiLoc();
        if(guiLoc == null)
            return;
        File file = new File(
                QuakeCraftReloaded.get().getDataFolder(),
                "guis" + File.separator + guiLoc + ".yml"
        );
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        loadGui(plugin, file.getName().replaceFirst("[.][^.]+$", ""), Config.wrap(config));
    }

    public void load() {
        loadConfig();
        loadGui(QuakeCraftReloaded.get());
    }
}
