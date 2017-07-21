package xyz.upperlevel.spigot.quakecraft.shop;

import lombok.Getter;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.gui.config.itemstack.CustomItem;

@Getter
public abstract class Purchase<T extends Purchase<T>> {
    private final PurchaseManager<T> manager;
    private final String id;
    private final String name;
    private final float cost;
    private final CustomItem icon;
    private final boolean def;

    public Purchase(PurchaseManager<T> manager, String id, String name, float cost, CustomItem icon, boolean def) {
        this.manager = manager;
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.icon = icon;
        this.def = def;
    }

    public Purchase(PurchaseManager<T> manager, String id, Config config) {
        this(
                manager,
                id,
                config.getStringRequired("name"),
                config.getInt("cost", 0),
                CustomItem.deserialize(config.getConfigRequired("icon")),
                config.getBool("default", false)
        );
    }
}
