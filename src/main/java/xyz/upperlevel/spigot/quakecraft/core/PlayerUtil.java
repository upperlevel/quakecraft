package xyz.upperlevel.spigot.quakecraft.core;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PlayerUtil {

    private PlayerUtil() {
    }

    public static void clearInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.saveData();
    }

    public static void playSound(Player player, Sound sound) {
        player.playSound(player.getLocation(), sound, 0f, 100f);
    }
}
