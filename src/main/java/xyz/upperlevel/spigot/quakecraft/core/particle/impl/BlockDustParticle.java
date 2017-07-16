package xyz.upperlevel.spigot.quakecraft.core.particle.impl;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import xyz.upperlevel.spigot.quakecraft.core.particle.Particle;
import xyz.upperlevel.spigot.quakecraft.core.particle.ParticleEffect;
import xyz.upperlevel.spigot.quakecraft.core.particle.ParticleType;

import java.util.Map;

@Getter
public class BlockDustParticle extends Particle {

    private Material blockType;
    private byte blockData;

    public BlockDustParticle() {
        super(ParticleType.BLOCK_DUST);

        setBlockType(Material.WOOL);
        setBlockData((byte) 0);
    }

    public BlockDustParticle(Map<String, Object> data) {
        super(ParticleType.BLOCK_DUST, data);

        setBlockType(Material.getMaterial((String) data.get("block.type")));
        setBlockData((Byte) data.get("block.data"));
    }

    public void setBlockType(Material blockType) {
        this.blockType = blockType == null ? Material.WOOL : blockType;
    }

    public void setBlockData(Byte blockData) {
        this.blockData = blockData == null || blockData <= 0 ? 0 : blockData;
    }

    @Override
    public void display(Location location) {
        ParticleEffect.BLOCK_DUST.display(
                new ParticleEffect.BlockData(blockType, blockData),
                getOffsetX(),
                getOffsetY(),
                getOffsetZ(),
                getSpeed(),
                getAmount(),
                location,
                getRange()
        );
    }
}
