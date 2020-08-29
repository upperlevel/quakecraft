package xyz.upperlevel.quakecraft.shop.dash;

import lombok.Getter;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.multi.MultiPurchaseManager;
import xyz.upperlevel.uppercore.config.Config;

public class DashPowerManager extends MultiPurchaseManager<DashPowerManager.DashPower> {

    public DashPowerManager(PurchaseRegistry registry) {
        super(registry, "dash-power");
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
    public void setSelected(Profile profile, DashPower purchase) {
        Quake.getProfileController().updateProfile(profile.getId(), new Profile().setSelectedDashPower(purchase));
    }

    @Override
    public DashPower getSelected(Profile profile) {
        return profile.getSelectedDashPower();
    }

    public class DashPower extends BaseDashUpgrade<DashPower> {
        @Getter
        private final float power;

        public DashPower(String id, Config config) {
            super(DashPowerManager.this, id, config);
            power = config.getFloatRequired("power");
        }

        @Override
        public boolean isSelected(Profile profile) {
            return profile.getSelectedDashPower() == this;
        }
    }
}
