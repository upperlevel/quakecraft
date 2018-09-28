package xyz.upperlevel.quakecraft.shop.purchase.multi;

import xyz.upperlevel.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseManager;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigException;

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
            } catch (InvalidConfigException e) {
                e.addLocation("in " + getPurchaseName() + " '" + entry.getKey() + "'");
                throw e;
            }
        }
    }
}
