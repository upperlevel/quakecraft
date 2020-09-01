package xyz.upperlevel.quakecraft.shop.dash;

import lombok.Getter;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.multi.MultiPurchaseManager;
import xyz.upperlevel.uppercore.config.Config;


public class DashCooldownManager extends MultiPurchaseManager<DashCooldownManager.DashCooldown> {

    public DashCooldownManager(PurchaseRegistry registry) {
        super(registry, "dash-cooldown");
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
    public void setSelected(Player player, DashCooldown purchase) {
        Quake.getProfileController().updateProfile(player.getUniqueId(), new Profile().setSelectedDashCooldown(purchase));
    }

    @Override
    public DashCooldown getSelected(Profile profile) {
        return profile.getSelectedDashCooldown();
    }

    public class DashCooldown extends BaseDashUpgrade<DashCooldown> {
        @Getter
        private final int cooldown;

        public DashCooldown(String id, Config config) {
            super(DashCooldownManager.this, id, config);
            cooldown = config.getIntRequired("cooldown");
        }

        @Override
        public boolean isSelected(Profile profile) {
            return profile.getSelectedDashCooldown() == this;
        }
    }
}
