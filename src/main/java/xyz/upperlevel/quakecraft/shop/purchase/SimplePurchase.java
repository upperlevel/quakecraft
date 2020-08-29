package xyz.upperlevel.quakecraft.shop.purchase;

import lombok.Getter;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.quakecraft.shop.require.Require;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.UItem;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.util.List;

@Getter
public abstract class SimplePurchase<T extends SimplePurchase<T>> extends Purchase<T> {
    private final UItem icon;

    public SimplePurchase(PurchaseManager<T> manager, String id, PlaceholderValue<String> name, float cost, UItem icon, boolean def, List<Require> requires) {
        super(manager, id, name, cost, def, requires);
        this.icon = icon;

        if (icon.getDisplayName() == null) {
            icon.setDisplayName(name);
            icon.setPlaceholders(getPlaceholders());
        }
    }

    @Override
    public UItem getIcon(Profile profile) {
        return icon;
    }

    public SimplePurchase(PurchaseManager<T> manager, String id, Config config) {
        super(manager, id, config);
        this.icon = config.getUItemRequired("icon");
    }
}
