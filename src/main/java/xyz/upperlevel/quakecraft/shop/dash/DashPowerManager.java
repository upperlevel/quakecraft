package xyz.upperlevel.quakecraft.shop.dash;

import lombok.Getter;
import xyz.upperlevel.quakecraft.QuakePlayer;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.multi.MultiPurchaseManager;
import xyz.upperlevel.uppercore.config.Config;

public class DashPowerManager extends MultiPurchaseManager<DashPowerManager.DashPower> {

    public DashPowerManager(PurchaseRegistry registry) {
        super(registry);
    }

    @Override
    public String getPartName() {
        return "power";
    }

    @Override
    public DashPower deserialize(String id, Config config) {
        return new DashPower(id, config);
    }

    @Override
    public void setSelected(QuakePlayer player, DashPower purchase) {
        player.setSelectedDashPower(purchase);
    }

    @Override
    public DashPower getSelected(QuakePlayer player) {
        return player.getSelectedDashPower();
    }

    @Override
    public String getPurchaseName() {
        return "dash_power";
    }

    public class DashPower extends BaseDashUpgrade<DashPower> {
        @Getter
        private final float power;

        public DashPower(String id, Config config) {
            super(DashPowerManager.this, id, config);
            power = config.getFloatRequired("power");
        }

        @Override
        public boolean isSelected(QuakePlayer player) {
            return player.getSelectedDashPower() == this;
        }
    }
}
