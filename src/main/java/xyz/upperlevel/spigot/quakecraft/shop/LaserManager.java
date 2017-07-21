package xyz.upperlevel.spigot.quakecraft.shop;

import lombok.Getter;
import org.bukkit.Color;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;
import xyz.upperlevel.uppercore.gui.config.itemstack.CustomItem;

public class LaserManager extends PurchaseManager<LaserManager.Laser> {

    @Override
    public Laser deserialize(String id, Config config) {
        try {
            return new Laser(id, config);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in laser \"" + id + "\"");
            throw e;
        }
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
    public class Laser extends Purchase<Laser>{
        private final Color fireworkColor;

        public Laser(String id, String name, float cost, CustomItem icon, boolean def, Color fireworkColor) {
            super(LaserManager.this, id, name, cost, icon, def);
            this.fireworkColor = fireworkColor;
        }

        protected Laser(String id, Config config) {
            super(LaserManager.this, id, config);
            this.fireworkColor = config.getColorRequired("fireworkColor");
        }
    }
}
