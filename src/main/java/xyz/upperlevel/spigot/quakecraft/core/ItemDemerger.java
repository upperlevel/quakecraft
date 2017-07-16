package xyz.upperlevel.spigot.quakecraft.core;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;

public class ItemDemerger implements Listener{
    private static int nextId = 0;

    static {
        Bukkit.getPluginManager().registerEvents(new EventListener(), QuakeCraftReloaded.get());
    }

    
    public static ItemStack setItem(ItemStack is) {
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("id:" + nextId++);
        is.setItemMeta(im);
        return is;
    }
    
    public static ItemStack clearItem(ItemStack is) {
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(null);
        is.setItemMeta(im);
        return is;
    }


    public static class EventListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerPickupItem(PlayerPickupItemEvent event) {
            ItemDemerger.clearItem(event.getItem().getItemStack());
        }
    }
}
