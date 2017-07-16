package xyz.upperlevel.spigot.quakecraft.core;

import org.bukkit.ChatColor;
import org.bukkit.Color;

import java.util.HashMap;
import java.util.Map;

public final class ColorUtil {

    private static final Map<String, Color> BY_NAME = new HashMap<String, Color>() {{
        put("WHITE", Color.WHITE);
        put("SILVER", Color.SILVER);
        put("GRAY", Color.GRAY);
        put("BLACK", Color.BLACK);
        put("RED", Color.RED);
        put("MAROON", Color.MAROON);
        put("YELLOW", Color.YELLOW);
        put("OLIVE", Color.OLIVE);
        put("LIME", Color.LIME);
        put("GREEN", Color.GREEN);
        put("AQUA", Color.AQUA);
        put("TEAL", Color.TEAL);
        put("BLUE", Color.BLUE);
        put("NAVY", Color.NAVY);
        put("FUCHSIA", Color.FUCHSIA);
        put("PURPLE", Color.PURPLE);
        put("ORANGE", Color.ORANGE);
    }};

    private ColorUtil() {
    }

    public static Color getColor(String name) {
        return BY_NAME.get(name);
    }
}
