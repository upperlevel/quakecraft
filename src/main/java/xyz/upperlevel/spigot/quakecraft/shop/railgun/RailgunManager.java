package xyz.upperlevel.spigot.quakecraft.shop.railgun;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.shop.gun.GunCategory;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.ConfigUtils;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;
import xyz.upperlevel.uppercore.gui.GuiId;

import java.io.File;
import java.util.*;


public class RailgunManager {
    private final GunCategory gunProvider;
    private List<Railgun> railguns = new ArrayList<>();
    private Multimap<Purchase<?>, Railgun> bySingleComponent = ArrayListMultimap.create(128, 5);
    private Map<List<? extends Purchase<?>>, Railgun> byComponents = new HashMap<>(28);
    @Getter
    private RailgunSelectGui gui;

    public RailgunManager(GunCategory gunProvider) {
        this.gunProvider = gunProvider;
    }


    public void register(Railgun railgun) {
        railguns.add(railgun);
        List<? extends Purchase<?>> components = railgun.getComponents();
        byComponents.put(components, railgun);
        for(Purchase<?> p : components)
            bySingleComponent.put(p, railgun);
    }

    public Railgun computeSelected(QuakePlayer player) {
        return byComponents.get(player.getGunComponents());
    }

    public Collection<Railgun> getUsedFor(Purchase<?> component) {
        return bySingleComponent.get(component);
    }

    public List<Railgun> getRailguns() {
        return Collections.unmodifiableList(railguns);
    }


    public void loadConfig(Map<String, Config> config) {
        for(Map.Entry<String, Config> entry : config.entrySet()) {
            try {
                register(new Railgun(entry.getKey(), gunProvider, entry.getValue()));
            } catch (InvalidConfigurationException e) {
                e.addLocalizer("in gun '" + entry.getKey() + "'");
                throw e;
            }
        }
        if(gui != null)
            gui.print();
    }

    public void loadConfig() {
        loadConfig(ConfigUtils.loadConfigMap(
                QuakeCraftReloaded.get(),
                "shop" + File.separator + "gun" + File.separator +  "guns.yml",
                "gun"
        ));
    }


    public void loadGui(Plugin plugin, Config config) {
        RailgunSelectGui gui;
        try {
            gui = new RailgunSelectGui(config, this);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in gun gui");
            throw e;
        }
        this.gui = gui;
        QuakeCraftReloaded.get().getGuis().register(new GuiId(plugin, "guns", gui));
        gui.print();
    }

    public void loadGui(Plugin plugin) {
        FileConfiguration config = ConfigUtils.loadConfig(
                QuakeCraftReloaded.get(),
                "guis" + File.separator + "guns.yml"
        );
        loadGui(plugin, Config.wrap(config));
    }

    public void load() {
        loadConfig();
        loadGui(QuakeCraftReloaded.get());
    }
}
