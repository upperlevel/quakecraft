package xyz.upperlevel.spigot.quakecraft.shop.armor;

import lombok.Getter;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.shop.Purchase;
import xyz.upperlevel.spigot.quakecraft.shop.PurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.gui.config.itemstack.CustomItem;

public class BootManager extends PurchaseManager<BootManager.Boot> {

    @Override
    public Boot deserialize(String id, Config config) {
        return new Boot(id, config);
    }

    @Override
    public String getGuiLoc() {
        return "boots";
    }

    @Override
    public String getConfigLoc() {
        return "armor/boots";
    }

    @Override
    public void setSelected(QuakePlayer player, Boot purchase) {
        player.setSelectedBoot(purchase);
    }

    @Override
    public Boot getSelected(QuakePlayer player) {
        return player.getSelectedBoot();
    }

    @Override
    public String getPurchaseName() {
        return "boot";
    }

    @Getter
    public class Boot extends Purchase<BootManager.Boot> {
        private final CustomItem item;

        public Boot(String id, String name, float cost, CustomItem icon, boolean def, CustomItem item) {
            super(BootManager.this, id, name, cost, icon, def);
            this.item = item;
        }

        protected Boot(String id, Config config) {
            super(BootManager.this, id, config);
            this.item = config.getCustomItem("item");
        }
    }
}
