package xyz.upperlevel.quakecraft.shop.require;

import org.bukkit.ChatColor;
import xyz.upperlevel.quakecraft.QuakeAccount;

public interface Require {
    String DONE = ChatColor.GREEN + "✔";
    String MISSING = ChatColor.RED + "✖";
    ChatColor DESCRIPTION_BASE = ChatColor.DARK_GRAY;
    ChatColor DESCRIPTION_VALUE = ChatColor.DARK_AQUA;
    ChatColor PROGRESS_BASE = ChatColor.GRAY;
    ChatColor PROGRESS_VALUE = ChatColor.DARK_AQUA;


    default String getRequires(QuakeAccount player) {
        return "Requires \"" + getName(player) + "\" " + getType() + "!";
    }

    String getName(QuakeAccount player);

    String getType();

    String getDescription();

    boolean test(QuakeAccount player);

    String getProgress();
}
