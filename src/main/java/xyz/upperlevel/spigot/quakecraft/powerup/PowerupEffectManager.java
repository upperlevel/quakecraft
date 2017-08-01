package xyz.upperlevel.spigot.quakecraft.powerup;

import xyz.upperlevel.spigot.quakecraft.powerup.effects.PowerupEffect;
import xyz.upperlevel.spigot.quakecraft.powerup.effects.RapidFirePowerupEffect;
import xyz.upperlevel.spigot.quakecraft.powerup.effects.SpeedPowerupEffect;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;

import java.util.*;

public class PowerupEffectManager {
    private static Map<String, PowerupEffect> effects = new HashMap<>();

    static {
        registerDef();
    }


    public static void register(PowerupEffect effect) {
        effects.put(effect.getId(), effect);
    }

    public static void registerDef() {
        register(new SpeedPowerupEffect());
        register(new RapidFirePowerupEffect());
    }

    public static Collection<PowerupEffect> get() {
        return effects.values();
    }

    public static PowerupEffect fromId(String id) {
        return effects.get(id);
    }

    public static Map<String, PowerupEffect> getById() {
        return Collections.unmodifiableMap(effects);
    }

    public static PowerupEffect getDef() {
        return effects.values().iterator().next();
    }

    public static void load(Config config) {
        Powerup.load();
        for(PowerupEffect effect : effects.values()) {
            try {
                effect.load(config.getConfigRequired(effect.getId()));
            } catch (InvalidConfigurationException e) {
                e.addLocalizer("in itembox '" + effect.getId() + "'");
                throw e;
            }
        }
    }
}
