package xyz.upperlevel.spigot.quakecraft.shop.gun;

import lombok.Getter;
import org.bukkit.FireworkEffect;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.shop.Purchase;
import xyz.upperlevel.spigot.quakecraft.shop.PurchaseManager;
import xyz.upperlevel.spigot.quakecraft.shop.PurchaseRegistry;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.CustomItem;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import static xyz.upperlevel.uppercore.config.ConfigUtils.parseFireworkEffectType;

public class BarrelManager extends PurchaseManager<BarrelManager.Barrel> {

    public BarrelManager(PurchaseRegistry registry) {
        super(registry);
    }

    @Override
    public Barrel deserialize(String id, Config config) {
        return new Barrel(id, config);
    }

    @Override
    public String getGuiLoc() {
        return "barrels";
    }

    @Override
    public String getConfigLoc() {
        return "gun/barrels";
    }

    @Override
    public void setSelected(QuakePlayer player, Barrel purchase) {
        player.setSelectedBarrel(purchase);
    }

    @Override
    public Barrel getSelected(QuakePlayer player) {
        return player.getSelectedBarrel();
    }

    @Override
    public String getPurchaseName() {
        return "barrel";
    }


    @Getter
    public class Barrel extends Purchase<Barrel> {
        private final FireworkEffect.Type fireworkType;

        protected Barrel(String id, Config config) {
            super(BarrelManager.this, id, config);
            this.fireworkType = parseFireworkEffectType(config.getStringRequired("firework-type"));
        }
    }
}
