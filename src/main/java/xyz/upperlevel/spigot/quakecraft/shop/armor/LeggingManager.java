package xyz.upperlevel.spigot.quakecraft.shop.armor;

import lombok.Getter;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.shop.Purchase;
import xyz.upperlevel.spigot.quakecraft.shop.PurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.gui.config.itemstack.CustomItem;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

public class LeggingManager extends PurchaseManager<LeggingManager.Legging> {

    @Override
    public Legging deserialize(String id, Config config) {
        return new Legging(id, config);
    }

    @Override
    public String getGuiLoc() {
        return "leggings";
    }

    @Override
    public String getConfigLoc() {
        return "armor/leggings";
    }

    @Override
    public void setSelected(QuakePlayer player, Legging purchase) {
        player.setSelectedLegging(purchase);
    }

    @Override
    public Legging getSelected(QuakePlayer player) {
        return player.getSelectedLegging();
    }

    @Override
    public String getPurchaseName() {
        return "legging";
    }

    @Getter
    public class Legging extends Purchase<Legging> {
        private final CustomItem item;

        public Legging(String id, PlaceholderValue<String> name, float cost, CustomItem icon, boolean def, CustomItem item) {
            super(LeggingManager.this, id, name, cost, icon, def);
            this.item = item;
        }

        protected Legging(String id, Config config) {
            super(LeggingManager.this, id, config);
            this.item = config.getCustomItem("item");
        }
    }
}

