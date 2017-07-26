package xyz.upperlevel.spigot.quakecraft.shop.purchase;

import lombok.Getter;
import lombok.Setter;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.uppercore.config.Config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class PurchaseManager<P extends Purchase<P>> {
    @Getter
    private final PurchaseRegistry registry;
    private Map<String, P> purchases = new LinkedHashMap<>();
    @Getter
    @Setter
    private PurchaseGui gui;
    private P def;

    public PurchaseManager(PurchaseRegistry registry) {
        this.registry = registry;
        if(registry != null)
            registry.register(this);
    }

    public void add(P item) {
        purchases.put(item.getId(), item);
        if(item.isDef()) {
            if(def != null)
                QuakeCraftReloaded.get().getLogger().warning("Multiple default values in " + getPurchaseName());
            def = item;
        }
        if(gui != null)
            gui.update();
    }

    public Map<String, P> getPurchases() {
        return Collections.unmodifiableMap(purchases);
    }

    public abstract P deserialize(String id, Config config);

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
