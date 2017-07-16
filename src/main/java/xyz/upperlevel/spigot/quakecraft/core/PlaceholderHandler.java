package xyz.upperlevel.spigot.quakecraft.core;

import lombok.Data;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.core.scoreboard.ScoreboardHandler;

public class PlaceholderHandler {

    private static boolean placeholderApi = false;

    static {
        placeholderApi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    private PlaceholderHandler() {
    }

    public static String replace(Player player, String text) {
        if (placeholderApi)
            PlaceholderAPI.setPlaceholders(player, text);

        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
