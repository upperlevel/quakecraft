package xyz.upperlevel.spigot.quakecraft.core.math;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class BoundingBox {

    //min and max points of hit box
    private final double minX;
    private final double maxX;
    private final double minY;
    private final double maxY;
    private final double minZ;
    private final double maxZ;

    public BoundingBox(Vector min, Vector max) {
        this.minX = min.getX();
        this.minY = min.getY();
        this.minZ = min.getZ();
        this.maxX = max.getX();
        this.maxY = max.getY();
        this.maxZ = max.getZ();
    }

    public BoundingBox(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    public Vector midPoint(){
        return new Vector(
                (minX + maxX) / 2.0,
                (minY + maxY) / 2.0,
                (minZ + maxZ) / 2.0
        );
    }

    public boolean isInside(Vector v) {
        return  v.getX() >= minX &&
                v.getX() <= maxX &&
                v.getZ() >= minZ &&
                v.getZ() <= maxZ &&
                v.getY() >= minY &&
                v.getY() <= maxY;
    }

    public boolean isInside(double x, double y, double z) {
        return  x >= minX &&
                x <= maxX &&
                z >= minZ &&
                z <= maxZ &&
                y >= minY &&
                y <= maxY;
    }

    public boolean isInside(Location l) {
        return  l.getX() >= minX &&
                l.getX() <= maxX &&
                l.getZ() >= minZ &&
                l.getZ() <= maxZ &&
                l.getY() >= minY &&
                l.getY() <= maxY;
    }

    public boolean intersects(BoundingBox b) {
        return  (b.minX <= maxX && b.maxX >= minX) &&
                (b.minZ <= maxZ && b.maxZ >= minZ) &&
                (b.minY <= maxY && b.maxY >= minY);
    }

}