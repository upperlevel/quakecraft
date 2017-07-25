package xyz.upperlevel.spigot.quakecraft.shop;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static xyz.upperlevel.spigot.quakecraft.core.CollectionUtil.toMap;

public abstract class PurchaseManager<P extends Purchase<P>> {
    private Map<String, P> purchases = new LinkedHashMap<>();
    @Getter
    private PurchasesGui<P> gui;
    private P def;

    public void add(P item) {
        purchases.put(item.getId(), item);
        if(item.isDef()) {
            if(def != null)
                QuakeCraftReloaded.get().getLogger().warning("Multiple default values in " + getPurchaseName());
            def = item;
        }
        if(gui != null)
            gui.setDirty();
    }

    public Map<String, P> getPurchases() {
        return Collections.unmodifiableMap(purchases);
    }

    public abstract P deserialize(String id, Config config);

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


    public void loadGui(Config config, String id) {
        gui = PurchasesGui.deserialize(QuakeCraftReloaded.get(), id, config, this);
        QuakeCraftReloaded.get().getGuis().register(gui.getId(), gui);
    }

    public void loadGui() {
        File file = new File(
                QuakeCraftReloaded.get().getDataFolder(),
                "guis" + File.separator + getGuiLoc() + ".yml"
        );
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        loadGui(Config.wrap(config), file.getName().replaceFirst("[.][^.]+$", ""));
    }

    public void load() {
        loadConfig();
        loadGui();
    }

    public boolean tryLoad() {
        try {
            load();
        } catch (InvalidConfigurationException e) {
            QuakeCraftReloaded.get().getLogger().severe(e.getErrorMessage("Error"));
            return false;
        }
        return true;
    }

    public abstract void setSelected(QuakePlayer player, P purchase);

    public abstract P getSelected(QuakePlayer player);

    public abstract String getPurchaseName();

    public P get(String name) {
        return purchases.get(name);
    }

    public P getDefault() {
        return def;
    }
}
