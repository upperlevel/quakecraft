package xyz.upperlevel.spigot.quakecraft.shop.purchase.multi;

import com.google.common.collect.Maps;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.shop.Category;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.PurchaseGui;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.gui.GuiId;

import java.io.File;
import java.util.*;

import static xyz.upperlevel.spigot.quakecraft.core.CollectionUtil.toMap;

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
        File file = new File(
                QuakeCraftReloaded.get().getDataFolder(),
                "shop" + File.separator + getConfigLoc() + ".yml"
        );
        loadConfig(Config.wrap(YamlConfiguration.loadConfiguration(file)));
    }

    private void loadConfig(Config config) {
        for (MultiPurchaseManager child : getChildren()) {
            child.loadConfig(
                    config.getSectionRequired(child.getPartName())
                            .entrySet()
                            .stream()
                            .map(e -> {
                                Object o = e.getValue();
                                if (o instanceof Map)
                                    return Maps.immutableEntry(e.getKey(), Config.wrap((Map<String, Object>) o));
                                else if (o instanceof ConfigurationSection)
                                    return Maps.immutableEntry(e.getKey(), Config.wrap((ConfigurationSection) o));
                                else {
                                    QuakeCraftReloaded.get().getLogger().severe("Cannot parse " + child.getPurchaseName() + e.getKey() + ": expected map (found: " + o.getClass().getSimpleName() + ")");
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .collect(toMap(LinkedHashMap::new))
            );
        }
    }

    protected abstract List<MultiPurchaseManager> getChildren();

    public abstract String getConfigLoc();
}
