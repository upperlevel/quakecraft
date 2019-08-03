package xyz.upperlevel.quakecraft.shop.gun;

import lombok.Getter;
import org.bukkit.FireworkEffect;
import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;

public class BarrelManager extends SinglePurchaseManager<BarrelManager.Barrel> {

    public BarrelManager(PurchaseRegistry registry) {
        super(registry);
    }

    @Override
    public Barrel deserialize(String id, Config config) {
        return new Barrel(id, config);
    }

    @Override
    public String getGuiLoc() {
        return "gun.barrels.gui";
    }

    @Override
    public String getConfigLoc() {
        return "gun.barrels.types";
    }

    @Override
    public void setSelected(QuakeAccount player, Barrel purchase) {
        player.setSelectedBarrel(purchase);
    }

    @Override
    public Barrel getSelected(QuakeAccount player) {
        return player.getSelectedBarrel();
    }

    @Override
    public String getPurchaseName() {
        return "barrel";
    }


    @Getter
    public class Barrel extends SimplePurchase<Barrel> {
        private final FireworkEffect.Type fireworkType;

        protected Barrel(String id, Config config) {
            super(BarrelManager.this, id, config);
            this.fireworkType = config.getRequired("firework-type", FireworkEffect.Type.class);
        }
    }
}
