package xyz.upperlevel.spigot.quakecraft.core.particle.data;

import org.bukkit.Material;
import xyz.upperlevel.spigot.quakecraft.core.particle.ParticleEffect;

/**
 * Represents the block data for the {@link ParticleEffect#BLOCK_CRACK} and {@link ParticleEffect#BLOCK_DUST} effects
 * <p>
 * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
 *
 * @author DarkBlade12
 * @since 1.6
 */
public class ParticleBlockData extends ParticleData {
    /**
     * Construct a new block data
     *
     * @param material Material of the block
     * @param data Data value of the block
     * @throws IllegalArgumentException If the material is not a block
     * @see ParticleData#ParticleData(Material, byte)
     */
    public ParticleBlockData(Material material, byte data) throws IllegalArgumentException {
        super(material, data);
        if (!material.isBlock())
            throw new IllegalArgumentException("The material + '" + material.name() + "' is not a block");
    }
}