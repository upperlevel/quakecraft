package xyz.upperlevel.spigot.quakecraft.shop;

import lombok.Getter;
import xyz.upperlevel.spigot.quakecraft.shop.require.Require;
import xyz.upperlevel.spigot.quakecraft.shop.require.RequireSystem;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.CustomItem;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.util.List;

@Getter
public abstract class Purchase<T extends Purchase<T>> {
    private final PurchaseManager<T> manager;
    private final String id;
    private final PlaceholderValue<String> name;
    private final float cost;
    private final CustomItem icon;
    private final boolean def;
    private PlaceholderRegistry placeholders;
    private List<Require> requires;

    public Purchase(PurchaseManager<T> manager, String id, PlaceholderValue<String> name, float cost, CustomItem icon, boolean def, List<Require> requires) {
        this.manager = manager;
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.icon = icon;
        this.def = def;

        if (icon.getDisplayName() == null) {
            icon.setDisplayName(name);
            icon.setPlaceholders(placeholders);
        }

        this.requires = requires;
    }

    public Purchase(PurchaseManager<T> manager, String id, Config config) {
        this.manager = manager;
        this.id = id;
        this.cost = config.getFloat("cost", 0.0f);
        this.name = config.getMessageRequired("name");
        this.def = config.getBool("default", false);
        this.placeholders = PlaceholderRegistry.create();
        fillPlaceholderSession(placeholders);
        this.icon = config.getCustomItemRequired("icon", placeholders);
        this.requires = RequireSystem.loadAll(this, config.get("requires"));
    }

    protected void fillPlaceholderSession(PlaceholderRegistry session) {
        session.set("cost", cost);
        session.set("name", name);
    }
}
