package xyz.upperlevel.spigot.quakecraft.shop;

import lombok.Getter;
import org.bukkit.FireworkEffect;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;
import xyz.upperlevel.uppercore.gui.config.itemstack.CustomItem;

import static xyz.upperlevel.uppercore.config.ConfigUtils.parseFireworkEffectType;

public class BarrelManager extends PurchaseManager<BarrelManager.Barrel> {

    @Override
    public Barrel deserialize(String id, Config config) {
        try {
            return new Barrel(id, config);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in barrel \"" + id + "\"");
            throw e;
        }
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
    public String getPurchaseName() {
        return "barrel";
    }


    @Getter
    public class Barrel extends Purchase<Barrel>{
        private final FireworkEffect.Type fireworkType;

        public Barrel(String id, String name, float cost, CustomItem icon, boolean def, FireworkEffect.Type fireworkType) {
            super(BarrelManager.this, id, name, cost, icon, def);
            this.fireworkType = fireworkType;
        }

        protected Barrel(String id, Config config) {
            super(BarrelManager.this, id, config);
            this.fireworkType = parseFireworkEffectType(config.getStringRequired("firework-type"));
        }
    }
}
