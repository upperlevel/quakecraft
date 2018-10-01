package xyz.upperlevel.quakecraft.shop.railgun;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.quakecraft.shop.gun.GunCategory;
import xyz.upperlevel.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigException;

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
        for (Purchase<?> p : components) {
            p.getUsedToMake().add(railgun);
            bySingleComponent.put(p, railgun);
        }
    }

    public Railgun computeSelected(QuakeAccount player) {
        return byComponents.get(player.getGunComponents());
    }

    public Collection<Railgun> getUsedFor(Purchase<?> component) {
        return bySingleComponent.get(component);
    }

    public List<Railgun> getRailguns() {
        return Collections.unmodifiableList(railguns);
    }


    public void loadConfig(Map<String, Config> config) {
        for (Map.Entry<String, Config> entry : config.entrySet()) {
            try {
                register(new Railgun(entry.getKey(), gunProvider, entry.getValue()));
            } catch (InvalidConfigException e) {
                e.addLocation("in gun '" + entry.getKey() + "'");
                throw e;
            }
        }
        if (gui != null)
            gui.print();
    }

    public void loadConfig() {
        Config config = Config.fromYaml(new File(
                Quake.get().getDataFolder(),
                "shop/gun/guns/guns.yml"
        ));
        loadConfig(config.asConfigMap());
    }


    public void loadGui(Config config) {
        RailgunSelectGui gui;
        try {
            gui = new RailgunSelectGui(config, this);
        } catch (InvalidConfigException e) {
            e.addLocation("in gun gui");
            throw e;
        }
        this.gui = gui;
        Quake.get().getGuis().register("guns_gui", gui);
        gui.print();
    }

    public void loadGui() {
        loadGui(Config.fromYaml(new File(
                Quake.get().getDataFolder(),
                "shop/gun/guns/guns_gui.yml"
        )));
    }

    public void load() {
        loadConfig();
        loadGui();
    }
}
