package xyz.upperlevel.spigot.quakecraft.core.particle.impl;

import xyz.upperlevel.spigot.quakecraft.core.particle.Particle;
import xyz.upperlevel.spigot.quakecraft.core.particle.ParticleEffect;
import xyz.upperlevel.spigot.quakecraft.core.particle.ParticleType;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;

public abstract class EffectParticle extends Particle {
    public EffectParticle(ParticleType type, ParticleEffect effect) {
        super(type);
        if(!effect.isSupported())
            throw new IllegalArgumentException("Unsupported particle: " + type.name());
    }

    public EffectParticle(ParticleType type, Config data, ParticleEffect effect) {
        super(type, data);
        if(!effect.isSupported())
            throw new InvalidConfigurationException("Unsupported particle: " + type.name());
    }
}
