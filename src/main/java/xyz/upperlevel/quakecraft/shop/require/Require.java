package xyz.upperlevel.quakecraft.shop.require;

import org.bukkit.ChatColor;
import xyz.upperlevel.quakecraft.profile.Profile;

public interface Require {
    ChatColor DESCRIPTION_BASE = ChatColor.DARK_GRAY;
    ChatColor DESCRIPTION_VALUE = ChatColor.DARK_AQUA;
    ChatColor PROGRESS_BASE = ChatColor.GRAY;
    ChatColor PROGRESS_VALUE = ChatColor.DARK_AQUA;

    String getName(Profile profile);

    String getType();

    String getDescription();

    boolean test(Profile profile);

    String getProgress();
}
