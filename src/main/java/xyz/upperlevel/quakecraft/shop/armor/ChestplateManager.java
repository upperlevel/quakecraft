package xyz.upperlevel.quakecraft.shop.armor;

import lombok.Getter;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.UItem;

public class ChestplateManager extends SinglePurchaseManager<ChestplateManager.Chestplate> {

    public ChestplateManager(PurchaseRegistry registry) {
        super(registry, "chestplate", "armor.kits");
    }

    @Override
    public Chestplate deserialize(String id, Config config) {
        return new Chestplate(id, config);
    }

    @Override
    public void setSelected(Player player, Chestplate purchase) {
        Quake.getProfileController().updateProfile(player.getUniqueId(), new Profile().setSelectedChestplate(purchase));
    }

    @Override
    public Chestplate getSelected(Profile profile) {
        return profile.getSelectedChestplate();
    }

    @Getter
    public class Chestplate extends SimplePurchase<Chestplate> {
        private final UItem item;

        protected Chestplate(String id, Config config) {
            super(ChestplateManager.this, id, config);
            this.item = config.getUItem("item", UItem.AIR);
        }
    }
}
