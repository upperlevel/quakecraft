package xyz.upperlevel.spigot.quakecraft.core.particle.impl;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import xyz.upperlevel.spigot.quakecraft.core.particle.ParticleType;
import xyz.upperlevel.spigot.quakecraft.core.particle.data.ParticleBlockData;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;

import static xyz.upperlevel.spigot.quakecraft.core.particle.ParticleEffect.BLOCK_DUST;

@Getter
public class BlockDustParticle extends EffectParticle {

    private Material blockType;
    private byte blockData;
    private ParticleBlockData data;

    public BlockDustParticle() {
        super(ParticleType.BLOCK_DUST, BLOCK_DUST);

        setBlockType(Material.WOOL);
        setBlockData((byte) 0);
        bake();
    }

    public BlockDustParticle(Config data) {
        super(ParticleType.BLOCK_DUST, data, BLOCK_DUST);
        Config block = data.getConfigRequired("block");
        Material type = block.getMaterialRequired("type");
        if(!type.isBlock())
            throw new InvalidConfigurationException("Particle must be a block!", "in particle '" + ParticleType.BLOCK_DUST.name() + "'");
        setBlockType(type);
        setBlockData(block.getByte("data", (byte)0));
        bake();
    }

    public void setBlockType(Material blockType) {
        this.blockType = blockType == null ? Material.WOOL : blockType;
    }

    public void setBlockData(byte blockData) {
        this.blockData = blockData;
    }

    public void bake() {
        data = new ParticleBlockData(blockType, blockData);
    }

    @Override
    public void display(Location location, Game game) {
        BLOCK_DUST.display(
                data,
                getOffsetX(),
                getOffsetY(),
                getOffsetZ(),
                getSpeed(),
                getAmount(),
                location,
                game.getPlayers()
        );
    }
}
