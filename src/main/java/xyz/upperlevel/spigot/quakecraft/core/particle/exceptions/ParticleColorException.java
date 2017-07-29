package xyz.upperlevel.spigot.quakecraft.core.particle.exceptions;

/**
 * Represents a runtime exception that is thrown either if the displayed particle effect is not colorable or if the particle color type is incorrect
 * <p>
 * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
 *
 * @author DarkBlade12
 * @since 1.7
 */
public class ParticleColorException extends RuntimeException {

    /**
     * Construct a new particle color exception
     *
     * @param message Message that will be logged
     */
    public ParticleColorException(String message) {
        super(message);
    }
}
