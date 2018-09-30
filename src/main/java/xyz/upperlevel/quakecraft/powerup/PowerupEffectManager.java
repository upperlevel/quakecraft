package xyz.upperlevel.quakecraft.powerup;

import org.yaml.snakeyaml.nodes.Tag;
import xyz.upperlevel.quakecraft.powerup.effects.PowerupEffect;
import xyz.upperlevel.quakecraft.powerup.effects.RapidFirePowerupEffect;
import xyz.upperlevel.quakecraft.powerup.effects.SpeedPowerupEffect;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.ConfigConstructor;
import xyz.upperlevel.uppercore.config.ConfigExternalDeclarator;
import xyz.upperlevel.uppercore.config.ConfigProperty;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigException;
import xyz.upperlevel.uppercore.config.parser.ConfigParserRegistry;

import java.util.*;

public class PowerupEffectManager {
    private static Map<String, PowerupEffect> effects = new HashMap<>();

    static {
        registerDef();
        ConfigParserRegistry.getStandard().registerFromDeclarator(new ConfigExternalDeclarator() {
            @ConfigConstructor(inlineable = true)
            public PowerupEffect parseEffect(@ConfigProperty String id) {
                PowerupEffect effect =  fromId(id);
                if (effect != null) return effect;
                throw new InvalidConfigException("Invalid powerup effect '" + id + "'");
            }
        });
        ConfigParserRegistry.getStandard().register(PowerupEffect.class, PowerupEffectManager::fromId, Tag.STR);
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
            } catch (InvalidConfigException e) {
                e.addLocation("in itembox '" + effect.getId() + "'");
                throw e;
            }
        }
    }
}
