package xyz.upperlevel.quakecraft.shop.gun;

import lombok.Getter;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.uppercore.config.Config;

public class TriggerManager extends SinglePurchaseManager<TriggerManager.Trigger> {

    public TriggerManager(PurchaseRegistry registry) {
        super(registry, "trigger", "gun.triggers");
    }

    @Override
    public Trigger deserialize(String id, Config config) {
        return new Trigger(id, config);
    }

    @Override
    public void setSelected(Profile profile, Trigger purchase) {
        profile.setSelectedTrigger(purchase);
    }

    @Override
    public Trigger getSelected(Profile profile) {
        return profile.getSelectedTrigger();
    }

    @Getter
    public class Trigger extends SimplePurchase<Trigger> {
        private final double firingSpeed;

        protected Trigger(String id, Config config) {
            super(TriggerManager.this, id, config);
            this.firingSpeed = config.getDoubleRequired("firing-speed");
        }
    }
}