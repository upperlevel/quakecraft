package xyz.upperlevel.spigot.quakecraft.core.math;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class ChunkBoundingBox {
    private final int minX;
    private final int maxX;
    private final int minZ;
    private final int maxZ;

    public ChunkBoundingBox(int minX, int maxX, int minZ, int maxZ) {
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    public boolean isInside(int x, int z) {
        return  x >= minX &&
                x <= maxX &&
                z >= minZ &&
                z <= maxZ;
    }

    public boolean isInside(Chunk chunk) {
        return  isInside(chunk.getX(), chunk.getZ());
    }

    public boolean isInside(Location loc) {
        return isInside(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    public boolean isInside(Entity entity) {
        Location loc = entity.getLocation();
        return isInside(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }
}
