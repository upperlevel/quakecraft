package xyz.upperlevel.spigot.quakecraft.core;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;

import java.util.List;

public class Configurator {

    private static final FileConfiguration config;

    static {
        config = QuakeCraftReloaded.get().getConfig();
    }

    private Configurator() {
    }

    public static ConfigurationSection getSection(String path) {
        return config.getConfigurationSection(path);
    }

    public static boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public static float getFloat(String path) {
        return (float) config.getDouble(path);
    }

    public static double getDouble(String path) {
        return config.getDouble(path);
    }

    public static int getInt(String path) {
        return config.getInt(path);
    }

    public static long getLong(String path) {
        return config.getLong(path);
    }

    public static String getString(String path) {
        return config.getString(path);
    }

    public static List<String> getStringList(String path) {
        return config.getStringList(path);
    }
}
