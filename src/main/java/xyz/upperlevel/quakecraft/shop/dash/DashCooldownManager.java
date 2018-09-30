package xyz.upperlevel.quakecraft.shop.dash;

import lombok.Getter;
import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.multi.MultiPurchaseManager;
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
    public void setSelected(QuakeAccount player, DashCooldown purchase) {
        player.setSelectedDashCooldown(purchase);
    }

    @Override
    public DashCooldown getSelected(QuakeAccount player) {
        return player.getSelectedDashCooldown();
    }

    @Override
    public String getPurchaseName() {
        return "dash_cooldown";
    }

    public class DashCooldown extends BaseDashUpgrade<DashCooldown> {
        @Getter
        private final int cooldown;

        public DashCooldown(String id, Config config) {
            super(DashCooldownManager.this, id, config);
            cooldown = config.getIntRequired("cooldown");
        }

        @Override
        public boolean isSelected(QuakeAccount player) {
            return player.getSelectedDashCooldown() == this;
        }
    }
}
