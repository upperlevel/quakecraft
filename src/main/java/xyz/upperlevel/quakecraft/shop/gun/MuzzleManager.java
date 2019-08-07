package xyz.upperlevel.quakecraft.shop.gun;

import lombok.Getter;
import xyz.upperlevel.quakecraft.QuakeAccount;
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
        super(registry, "gun.muzzles");
    }

    @Override
    public Muzzle deserialize(String id, Config config) {
        return new Muzzle(id, config);
    }

    @Override
    public void setSelected(QuakeAccount player, Muzzle purchase) {
        player.setSelectedMuzzle(purchase);
    }

    @Override
    public Muzzle getSelected(QuakeAccount player) {
        return player.getSelectedMuzzle();
    }

    @Override
    public String getPurchaseName() {
        return "muzzle";
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
