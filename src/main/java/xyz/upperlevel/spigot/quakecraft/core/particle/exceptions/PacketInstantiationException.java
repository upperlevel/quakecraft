package xyz.upperlevel.spigot.quakecraft.core.particle.exceptions;

/**
 * Represents a runtime exception that is thrown if packet instantiation fails
 * <p>
 * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
 *
 * @author DarkBlade12
 * @since 1.4
 */
public class PacketInstantiationException extends RuntimeException {
    /**
     * Construct a new packet instantiation exception
     *
     * @param message Message that will be logged
     * @param cause Cause of the exception
     */
    public PacketInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }
}