package xyz.upperlevel.spigot.quakecraft.shop.gun;

import lombok.Getter;
import org.bukkit.Color;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.shop.Purchase;
import xyz.upperlevel.spigot.quakecraft.shop.PurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.CustomItem;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

public class LaserManager extends PurchaseManager<LaserManager.Laser> {

    @Override
    public Laser deserialize(String id, Config config) {
        return new Laser(id, config);
    }

    @Override
    public String getGuiLoc() {
        return "lasers";
    }

    @Override
    public String getConfigLoc() {
        return "gun/lasers";
    }

    @Override
    public void setSelected(QuakePlayer player, Laser purchase) {
        player.setSelectedLaser(purchase);
    }

    @Override
    public Laser getSelected(QuakePlayer player) {
        return player.getSelectedLaser();
    }

    @Override
    public String getPurchaseName() {
        return "laser";
    }


    @Getter
    public class Laser extends Purchase<Laser> {
        private final Color fireworkColor;

        public Laser(String id, PlaceholderValue<String> name, float cost, CustomItem icon, boolean def, Color fireworkColor) {
            super(LaserManager.this, id, name, cost, icon, def);
            this.fireworkColor = fireworkColor;
        }

        protected Laser(String id, Config config) {
            super(LaserManager.this, id, config);
            this.fireworkColor = config.getColorRequired("firework-color");
        }
    }
}
