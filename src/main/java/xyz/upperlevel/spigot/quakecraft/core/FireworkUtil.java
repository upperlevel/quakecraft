package xyz.upperlevel.spigot.quakecraft.core;

import org.bukkit.FireworkEffect;

import java.util.HashMap;
import java.util.Map;

public final class FireworkUtil {

    private FireworkUtil() {
    }

    private static final Map<String, FireworkEffect.Type> TYPE_BY_NAME = new HashMap<>();

    static {
        for (FireworkEffect.Type value : FireworkEffect.Type.values())
            TYPE_BY_NAME.put(value.name(), value);
    }

    public static FireworkEffect.Type getFireworkType(String name) {
        return TYPE_BY_NAME.get(name);
    }
}
