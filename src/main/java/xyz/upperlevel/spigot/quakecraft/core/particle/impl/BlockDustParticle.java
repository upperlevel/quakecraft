package xyz.upperlevel.spigot.quakecraft.core.particle.impl;

import lombok.Getter;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import xyz.upperlevel.spigot.quakecraft.core.particle.Particle;
import xyz.upperlevel.spigot.quakecraft.core.particle.ParticleEffect;
import xyz.upperlevel.spigot.quakecraft.core.particle.ParticleType;
import xyz.upperlevel.spigot.quakecraft.core.particle.data.ParticleBlockData;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.uppercore.config.Config;

@Getter
public class BlockDustParticle extends Particle {

    private Material blockType;
    private byte blockData;
    private ParticleBlockData data;

    public BlockDustParticle() {
        super(ParticleType.BLOCK_DUST);

        setBlockType(Material.WOOL);
        setBlockData((byte) 0);
        bake();
    }

    public BlockDustParticle(Config data) {
        super(ParticleType.BLOCK_DUST, data);
        Config block = data.getConfigRequired("block");
        setBlockType(block.getMaterialRequired("type"));
        setBlockData(block.getByte("data", (byte)0));
    }

    public void setBlockType(Material blockType) {
        this.blockType = blockType == null ? Material.WOOL : blockType;
    }

    public void setBlockData(byte blockData) {
        this.blockData = blockData;
    }

    private void bake() {
        data = new ParticleBlockData(blockType, blockData);
    }

    @Override
    public void display(Location location, Game game) {
        ParticleEffect.BLOCK_DUST.display(
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
