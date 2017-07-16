package xyz.upperlevel.spigot.quakecraft.core.particle.impl;

import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import xyz.upperlevel.spigot.quakecraft.core.ColorUtil;
import xyz.upperlevel.spigot.quakecraft.core.particle.Particle;
import xyz.upperlevel.spigot.quakecraft.core.particle.ParticleEffect;
import xyz.upperlevel.spigot.quakecraft.core.particle.ParticleType;

import java.util.HashMap;
import java.util.Map;

@Getter
public class SimpleParticle extends Particle {

    private Color color;

    public SimpleParticle() {
        super(ParticleType.SIMPLE);

        setColor(Color.WHITE);
    }

    public SimpleParticle(Map<String, Object> data) {
        super(ParticleType.SIMPLE, data);

        String color = (String) data.get("color");
        if (color != null)
            color = color.toUpperCase();
        setColor(ColorUtil.getColor(color));
    }

    public void setColor(Color color) {
        this.color = color == null ? Color.WHITE : color;
    }

    @Override
    public void display(Location location) {
        ParticleEffect.REDSTONE.display(
                new ParticleEffect.OrdinaryColor(
                        color.getRed(),
                        color.getBlue(),
                        color.getGreen()
                ),
                location
        );
    }
}
