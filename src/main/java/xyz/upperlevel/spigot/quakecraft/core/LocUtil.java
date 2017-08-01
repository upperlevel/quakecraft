package xyz.upperlevel.spigot.quakecraft.core;

import org.bukkit.Location;

public final class LocUtil {

    public static String format(Location loc, boolean world) {
        return (world ? (loc.getWorld().getName() + ":") : "") + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    private LocUtil() {}
}
