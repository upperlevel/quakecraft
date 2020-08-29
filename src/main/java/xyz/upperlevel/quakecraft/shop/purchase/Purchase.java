package xyz.upperlevel.quakecraft.shop.purchase;

import lombok.Getter;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.quakecraft.shop.railgun.Railgun;
import xyz.upperlevel.quakecraft.shop.require.Require;
import xyz.upperlevel.quakecraft.shop.require.RequireSystem;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.economy.EconomyManager;
import xyz.upperlevel.uppercore.itemstack.UItem;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class Purchase<T extends Purchase<T>> {
    private final PurchaseManager<T> manager;
    private final String id;
    private final PlaceholderValue<String> name;
    private final float cost;
    private final boolean def;
    private PlaceholderRegistry placeholders;
    private List<Require> requires;
    private List<Railgun> usedToMake = new ArrayList<>();

    public Purchase(PurchaseManager<T> manager, String id, PlaceholderValue<String> name, float cost, boolean def, List<Require> requires) {
        this.manager = manager;
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.def = def;
        this.requires = requires;
    }

    public Purchase(PurchaseManager<T> manager, String id, Config config) {
        this.manager = manager;
        this.id = id;
        this.cost = config.getFloat("cost", 0.0f);
        this.name = config.getMessageStrRequired("name");
        this.def = config.getBool("default", false);
        this.placeholders = PlaceholderRegistry.create();
        fillPlaceholderSession(placeholders);
        this.requires = RequireSystem.loadAll(this, config.get("requires", RequireSystem.requireConfType, null));
    }

    public String getFullId() {
        return manager.getPurchaseName() + ":" + id;
    }

    public abstract UItem getIcon(Profile profile);

    protected void fillPlaceholderSession(PlaceholderRegistry session) {
        session.set("cost", getCostFormatted());
        session.set("name", name);
    }

    public String getCostFormatted() {
        return EconomyManager.format(cost);
    }

    public boolean isSelected(Profile profile) {
        return manager.getSelected(profile) == this;
    }

    @Override
    public String toString() {
        return getFullId();
    }
}
