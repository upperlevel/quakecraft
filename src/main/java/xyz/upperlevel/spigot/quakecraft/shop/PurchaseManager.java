package xyz.upperlevel.spigot.quakecraft.shop;

import com.google.common.collect.Maps;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.uppercore.config.Config;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class PurchaseManager<P extends Purchase> {
    private Map<String, P> purchases = new HashMap<>();
    private PurchasesGui<P> gui;

    public void add(P item) {
        purchases.put(item.getId(), item);
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
            add(deserialize(entry.getKey(), entry.getValue()));
        }
    }

    public void loadConfig(File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        loadConfig(config.getValues(true)
                .entrySet()
                .stream()
                .map(e -> {
                    Object o = e.getValue();
                    if(o instanceof Map)
                        return Maps.immutableEntry(e.getKey(), Config.wrap((Map)o));
                    else {
                        QuakeCraftReloaded.get().getLogger().severe("Cannot parse " + getPurchaseName() + e.getKey() + ": expected map");
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public void loadConfig() {
        File file = new File(
                QuakeCraftReloaded.get().getDataFolder(),
                "shop" + File.pathSeparator + getConfigLoc()
        );
    }


    public void loadGui(Config config) {
        gui = PurchasesGui.deserialize(QuakeCraftReloaded.get(), getPurchaseName(), config, this);
        QuakeCraftReloaded.get().getGuis().register(gui.getId(), gui);
    }

    public void loadGui() {
        File file = new File(
                QuakeCraftReloaded.get().getDataFolder(),
                "guis" + File.pathSeparator + getGuiLoc() + ".yml"
        );
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        loadGui(Config.wrap(config));
    }

    public void load() {
        loadConfig();
        loadGui();
    }

    public abstract String getPurchaseName();

    public P get(String name) {
        return purchases.get(name);
    }
}
