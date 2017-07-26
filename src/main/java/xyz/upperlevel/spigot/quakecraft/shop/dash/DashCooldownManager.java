package xyz.upperlevel.spigot.quakecraft.shop.dash;

import lombok.Getter;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.multi.MultiPurchaseManager;
import xyz.upperlevel.uppercore.config.Config;


public class DashCooldownManager extends MultiPurchaseManager<DashCooldownManager.DashCooldown> {

    public DashCooldownManager(PurchaseRegistry registry) {
        super(registry);
    }

    @Override
    public String getPartName() {
        return "cooldown";
    }

    @Override
    public DashCooldown deserialize(String id, Config config) {
        return new DashCooldown(id, config);
    }

    @Override
    public void setSelected(QuakePlayer player, DashCooldown purchase) {
        player.setSelectedDashCooldown(purchase);
    }

    @Override
    public DashCooldown getSelected(QuakePlayer player) {
        return player.getSelectedDashCooldown();
    }

    @Override
    public String getPurchaseName() {
        return "dash cooldown";
    }

    public class DashCooldown extends BaseDashUpgrade<DashCooldown> {
        @Getter
        private final float cooldown;

        public DashCooldown(String id, Config config) {
            super(DashCooldownManager.this, id, config);
            cooldown = config.getFloatRequired("cooldown");
        }

        @Override
        public boolean isSelected(QuakePlayer player) {
            return player.getSelectedDashCooldown() == this;
        }
    }
}
