package xyz.upperlevel.spigot.quakecraft.shop.purchase.multi;

import xyz.upperlevel.spigot.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.PurchaseManager;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;

import java.util.Map;

public abstract class MultiPurchaseManager<T extends Purchase<T>> extends PurchaseManager<T> {
    public MultiPurchaseManager(PurchaseRegistry registry) {
        super(registry);
    }

    public abstract String getPartName();

    public void loadConfig(Map<String, Config> config) {
        for(Map.Entry<String, Config> entry : config.entrySet()) {
            try {
                add(deserialize(entry.getKey(), entry.getValue()));
            } catch (InvalidConfigurationException e) {
                e.addLocalizer("in " + getPurchaseName() + " '" + entry.getKey() + "'");
                throw e;
            }
        }
    }
}
