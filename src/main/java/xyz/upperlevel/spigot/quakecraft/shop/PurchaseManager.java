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

    public void add(P item) {
        purchases.put(item.getId(), item);
    }

    public void load(Map<String, Config> config) {
        for(Map.Entry<String, Config> entry : config.entrySet()) {
            add(deserialize(entry.getKey(), entry.getValue()));
        }
    }

    public void load(File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        load(config.getValues(true)
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

    public Map<String, P> getPurchases() {
        return Collections.unmodifiableMap(purchases);
    }

    public abstract P deserialize(String id, Config config);

    public abstract String getPurchaseName();
}
