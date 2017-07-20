package xyz.upperlevel.spigot.quakecraft.shop;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.FireworkEffect;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.gui.config.itemstack.CustomItem;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static xyz.upperlevel.uppercore.config.ConfigUtils.parseFireworkEffectType;

public class BarrelManager {
    private Map<String, Barrel> barrels = new HashMap<>();

    public void add(Barrel barrel) {
        barrels.put(barrel.id, barrel);
    }

    public void load(Map<String, Config> config) {
        for(Map.Entry<String, Config> entry : config.entrySet()) {
            add(Barrel.deserialize(entry.getKey(), entry.getValue()));
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
                        QuakeCraftReloaded.get().getLogger().severe("Cannot parse barrel " + e.getKey() + ": expected map");
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public Map<String, Barrel> getBarrels() {
        return Collections.unmodifiableMap(barrels);
    }


    @RequiredArgsConstructor
    @Getter
    public static class Barrel {
        private final String id;
        private final String displayName;
        private final boolean def;
        private final CustomItem icon;
        private final FireworkEffect.Type fireworkType;


        public static Barrel deserialize(String id, Config config) {
            return new Barrel(
                    id,
                    config.getStringRequired("name"),
                    config.getBool("default", false),
                    CustomItem.deserialize(config.getConfigRequired("icon")),
                    parseFireworkEffectType(config.getStringRequired("firework-type"))
            );
        }
    }
}
