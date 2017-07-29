package xyz.upperlevel.spigot.quakecraft.core.particle.impl;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Color;
import org.bukkit.Location;
import xyz.upperlevel.spigot.quakecraft.core.particle.ParticleColor;
import xyz.upperlevel.spigot.quakecraft.core.particle.ParticleType;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.uppercore.config.Config;

import static xyz.upperlevel.spigot.quakecraft.core.particle.ParticleEffect.REDSTONE;

public class SimpleParticle extends EffectParticle {

    @Getter
    @Setter
    private ParticleColor color;

    public SimpleParticle() {
        super(ParticleType.SIMPLE, REDSTONE);

        setColor(Color.WHITE);
    }

    public SimpleParticle(Config data) {
        super(ParticleType.SIMPLE, data, REDSTONE);
        setColor(data.getColor("color", Color.WHITE));
    }

    public void setColor(Color color) {
        this.color = ParticleColor.of(color);
    }

    @Override
    public void display(Location loc, Game game) {
        REDSTONE.display(
                color,
                loc,
                game.getPlayers()
        );
    }
}
