package xyz.upperlevel.quakecraft.shop.gun;

import lombok.Getter;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.particle.CustomParticle;
import xyz.upperlevel.uppercore.util.TypeUtil;

import java.lang.reflect.Type;
import java.util.List;

public class MuzzleManager extends SinglePurchaseManager<MuzzleManager.Muzzle> {
    private static final Type particleListType = TypeUtil.typeOf(List.class, CustomParticle.class);

    public MuzzleManager(PurchaseRegistry registry) {
        super(registry, "muzzle", "gun.muzzles");
    }

    @Override
    public Muzzle deserialize(String id, Config config) {
        return new Muzzle(id, config);
    }

    @Override
    public void setSelected(Player player, Muzzle purchase) {
        Quake.getProfileController().updateProfile(player.getUniqueId(), new Profile().setSelectedMuzzle(purchase));
    }

    @Override
    public Muzzle getSelected(Profile profile) {
        return profile.getSelectedMuzzle();
    }

    @Getter
    public class Muzzle extends SimplePurchase<Muzzle> {
        private final List<CustomParticle> particles;


        protected Muzzle(String id, Config config) {
            super(MuzzleManager.this, id, config);
            // Magic :3
            this.particles = config.get("particles", particleListType, null);
        }
    }
}
