package xyz.upperlevel.spigot.quakecraft.core.particle.data;

import org.bukkit.Material;
import xyz.upperlevel.spigot.quakecraft.core.particle.ParticleEffect;

/**
 * Represents the particle data for effects like {@link ParticleEffect#ITEM_CRACK}, {@link ParticleEffect#BLOCK_CRACK} and {@link ParticleEffect#BLOCK_DUST}
 * <p>
 * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
 *
 * @author DarkBlade12
 * @since 1.6
 */
public abstract class ParticleData {
    private final Material material;
    private final byte data;
    private final int[] packetData;

    /**
     * Construct a new particle data
     *
     * @param material Material of the item/block
     * @param data Data value of the item/block
     */
    @SuppressWarnings("deprecation")
    public ParticleData(Material material, byte data) {
        this.material = material;
        this.data = data;
        this.packetData = new int[] { material.getId(), data };
    }

    /**
     * Returns the material of this data
     *
     * @return The material
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Returns the data value of this data
     *
     * @return The data value
     */
    public byte getData() {
        return data;
    }

    /**
     * Returns the data as an int array for packet construction
     *
     * @return The data for the packet
     */
    public int[] getPacketData() {
        return packetData;
    }

    /**
     * Returns the data as a string for pre 1.8 versions
     *
     * @return The data string for the packet
     */
    public String getPacketDataString() {
        return "_" + packetData[0] + "_" + packetData[1];
    }
}