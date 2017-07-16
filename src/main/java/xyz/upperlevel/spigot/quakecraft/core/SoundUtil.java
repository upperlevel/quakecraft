package xyz.upperlevel.spigot.quakecraft.core;

import org.bukkit.Sound;

import java.util.HashMap;
import java.util.Map;

public final class SoundUtil {

    private SoundUtil() {
    }

    private static final Map<String, Sound> BY_NAME = new HashMap<>();

    static {
        for (Sound sound : Sound.values())
            BY_NAME.put(sound.name(), sound);
    }

    public static Sound getSound(String name) {
        return BY_NAME.get(name);
    }
}
