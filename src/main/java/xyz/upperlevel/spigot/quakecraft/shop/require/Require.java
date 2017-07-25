package xyz.upperlevel.spigot.quakecraft.shop.require;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;

public interface Require {
    String DONE = ChatColor.GREEN + "✔";
    String MISSING = ChatColor.RED + "✘";
    ChatColor DESCRIPTION_BASE = ChatColor.DARK_GRAY;
    ChatColor DESCRIPTION_VALUE = ChatColor.DARK_AQUA;
    ChatColor PROGRESS_BASE = ChatColor.GRAY;
    ChatColor PROGRESS_VALUE = ChatColor.DARK_AQUA;


    default String getRequires(QuakePlayer player) {
        return "Requires \"" + name(player) + "\" " + type() + "!";
    }

    String name(QuakePlayer player);

    String type();

    String description();

    boolean test(QuakePlayer player);

    String getProgress();
}
