package xyz.upperlevel.spigot.quakecraft.shop.armor;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.CustomItem;

public class BootManager extends SinglePurchaseManager<BootManager.Boot> {

    public BootManager(PurchaseRegistry registry) {
        super(registry);
    }

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
    public class Boot extends SimplePurchase<Boot> {
        private final CustomItem item;

        protected Boot(String id, Config config) {
            super(BootManager.this, id, config);
            this.item = config.getCustomItem("item", CustomItem.AIR);
        }
    }
}
