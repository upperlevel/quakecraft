package xyz.upperlevel.spigot.quakecraft.core.particle;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.bukkit.Color;

/**
 * Represents the color for effects like {@link ParticleEffect#SPELL_MOB}, {@link ParticleEffect#SPELL_MOB_AMBIENT}, {@link ParticleEffect#REDSTONE} and {@link ParticleEffect#NOTE}
 * <p>
 * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
 *
 * @author DarkBlade12
 * @since 1.7
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ParticleColor {
    public float valueX;
    public float valueY;
    public float valueZ;

    public static ParticleColor of(Color color) {
        return new ParticleColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
    }

    public static ParticleColor of(int r, int g, int b) {
        return new ParticleColor(r / 255f, g / 255f, b / 255f);
    }

    public static ParticleColor of(float r, float g, float b) {
        return new ParticleColor(r, g, b);
    }

    public static ParticleColor ofNote(int note) {
        return new ParticleColor(note / 24f, 0f, 0f);
    }
}
