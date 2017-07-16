package xyz.upperlevel.spigot.quakecraft.core.particle;

import org.bukkit.configuration.ConfigurationSection;
import xyz.upperlevel.spigot.quakecraft.core.particle.impl.BlockDustParticle;
import xyz.upperlevel.spigot.quakecraft.core.particle.impl.SimpleParticle;

import java.util.HashMap;
import java.util.Map;

public enum ParticleType {

    SIMPLE {
        @Override
        public Particle create() {
            return new SimpleParticle();
        }

        @Override
        public Particle create(Map<String, Object> data) {
            return new SimpleParticle(data);
        }
    },
    BLOCK_DUST {
        @Override
        public Particle create() {
            return new BlockDustParticle();
        }

        @Override
        public Particle create(Map<String, Object> data) {
            return new BlockDustParticle(data);
        }
    };

    public abstract Particle create();

    public abstract Particle create(Map<String, Object> data);

    private static final Map<String, ParticleType> BY_NAME = new HashMap<>();

    static {
        for (ParticleType value : values())
            BY_NAME.put(value.name(), value);
    }

    public static ParticleType get(String name) {
        return BY_NAME.get(name);
    }
}
