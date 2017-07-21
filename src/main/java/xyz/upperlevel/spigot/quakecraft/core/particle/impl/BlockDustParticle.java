package xyz.upperlevel.spigot.quakecraft.core.particle.impl;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import xyz.upperlevel.spigot.quakecraft.core.particle.Particle;
import xyz.upperlevel.spigot.quakecraft.core.particle.ParticleEffect;
import xyz.upperlevel.spigot.quakecraft.core.particle.ParticleType;
import xyz.upperlevel.uppercore.config.Config;

@Getter
public class BlockDustParticle extends Particle {

    private Material blockType;
    private byte blockData;

    public BlockDustParticle() {
        super(ParticleType.BLOCK_DUST);

        setBlockType(Material.WOOL);
        setBlockData((byte) 0);
    }

    public BlockDustParticle(Config data) {
        super(ParticleType.BLOCK_DUST, data);

        setBlockType(data.getMaterialRequired("block.type"));
        setBlockData(data.getByte("block.data"));
    }

    public void setBlockType(Material blockType) {
        this.blockType = blockType == null ? Material.WOOL : blockType;
    }

    public void setBlockData(byte blockData) {
        this.blockData = blockData;
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
