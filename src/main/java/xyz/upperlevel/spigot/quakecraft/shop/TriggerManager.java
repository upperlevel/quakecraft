package xyz.upperlevel.spigot.quakecraft.shop;

import lombok.Getter;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;
import xyz.upperlevel.uppercore.gui.config.itemstack.CustomItem;

public class TriggerManager extends PurchaseManager<TriggerManager.Trigger> {

    @Override
    public Trigger deserialize(String id, Config config) {
        try {
            return new Trigger(id, config);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in trigger \"" + id + "\"");
            throw e;
        }
    }

    @Override
    public String getGuiLoc() {
        return "triggers";
    }

    @Override
    public String getConfigLoc() {
        return "gun/triggers";
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
    public class Trigger extends Purchase<Trigger>{
        private final double firingSpeed;

        public Trigger(String id, String name, float cost, CustomItem icon, boolean def, double firingSpeed) {
            super(TriggerManager.this, id, name, cost, icon, def);
            this.firingSpeed = firingSpeed;
        }

        protected Trigger(String id, Config config) {
            super(TriggerManager.this, id, config);
            this.firingSpeed = config.getDoubleRequired("firing-speed");
        }
    }
}