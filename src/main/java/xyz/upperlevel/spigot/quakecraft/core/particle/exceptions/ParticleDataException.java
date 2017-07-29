package xyz.upperlevel.spigot.quakecraft.core.particle.exceptions;

/**
 * Represents a runtime exception that is thrown either if the displayed particle effect requires data and has none or vice-versa or if the data type is incorrect
 * <p>
 * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
 *
 * @author DarkBlade12
 * @since 1.6
 */
public class ParticleDataException extends RuntimeException {

    /**
     * Construct a new particle data exception
     *
     * @param message Message that will be logged
     */
    public ParticleDataException(String message) {
        super(message);
    }
}
