package xyz.upperlevel.spigot.quakecraft.core.math;

import org.bukkit.util.Vector;

public class BoundingBox {

    //min and max points of hit box
    Vector max;
    Vector min;

    BoundingBox(Vector min, Vector max) {
        this.max = max;
        this.min = min;
    }

    public Vector midPoint(){
        return max.clone().add(min).multiply(0.5);
    }

}