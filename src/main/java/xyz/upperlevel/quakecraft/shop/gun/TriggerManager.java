package xyz.upperlevel.quakecraft.shop.gun;

import lombok.Getter;
import xyz.upperlevel.quakecraft.QuakePlayer;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.uppercore.config.Config;

public class TriggerManager extends SinglePurchaseManager<TriggerManager.Trigger> {

    public TriggerManager(PurchaseRegistry registry) {
        super(registry);
    }

    @Override
    public Trigger deserialize(String id, Config config) {
        return new Trigger(id, config);
    }

    @Override
    public String getGuiLoc() {
        return "gun/triggers/triggers_gui";
    }

    @Override
    public String getConfigLoc() {
        return "gun/triggers/triggers";
    }

    @Override
    public void setSelected(QuakePlayer player, Trigger purchase) {
        player.setSelectedTrigger(purchase);
    }

    @Override
    public Trigger getSelected(QuakePlayer player) {
        return player.getSelectedTrigger();
    }

    @Override
    public String getPurchaseName() {
        return "trigger";
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