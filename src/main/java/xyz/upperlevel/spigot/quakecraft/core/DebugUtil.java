package xyz.upperlevel.spigot.quakecraft.core;

import org.bukkit.Bukkit;

import java.util.logging.Level;

import static org.bukkit.ChatColor.*;

public final class DebugUtil {

    private DebugUtil() {
    }

    public static void printInfo(String msg) {
        Bukkit.getConsoleSender().sendMessage(BLUE + msg);
    }

    public static void printSuccess(String msg) {
        Bukkit.getConsoleSender().sendMessage(GREEN + msg);
    }

    public static void printWarning(String msg) {
        Bukkit.getConsoleSender().sendMessage(YELLOW + msg);
    }

    public static void printError(String msg) {
        Bukkit.getConsoleSender().sendMessage(RED + msg);
    }
}
