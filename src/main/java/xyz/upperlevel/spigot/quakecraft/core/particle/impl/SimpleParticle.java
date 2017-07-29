package xyz.upperlevel.spigot.quakecraft.core.particle.impl;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.spigot.quakecraft.core.math.BoundingBox;
import xyz.upperlevel.spigot.quakecraft.core.particle.Particle;
import xyz.upperlevel.spigot.quakecraft.core.particle.ParticleColor;
import xyz.upperlevel.spigot.quakecraft.core.particle.ParticleEffect;
import xyz.upperlevel.spigot.quakecraft.core.particle.ParticleType;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.spigot.quakecraft.game.GamePhase;
import xyz.upperlevel.spigot.quakecraft.game.Participant;
import xyz.upperlevel.spigot.quakecraft.game.play.PlayingPhase;
import xyz.upperlevel.uppercore.config.Config;

@Getter
public class SimpleParticle extends Particle {

    private Color color;

    public SimpleParticle() {
        super(ParticleType.SIMPLE);

        setColor(Color.WHITE);
    }

    public SimpleParticle(Config data) {
        super(ParticleType.SIMPLE, data);
        setColor(data.getColor("color", Color.WHITE));
    }

    public void setColor(Color color) {
        this.color = color == null ? Color.WHITE : color;
    }

    @Override
    public void display(Location loc, Game game) {
        ParticleEffect.REDSTONE.display(
                ParticleColor.of(color),
                loc,
                game.getPlayers()
        );
    }
}
