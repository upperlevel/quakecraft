package xyz.upperlevel.quakecraft.arena;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.util.concurrent.ThreadLocalRandom;

public class QuakeArenaListener implements Listener {
    private final QuakeArena arena;

    public QuakeArenaListener(QuakeArena arena) {
        this.arena = arena;
    }

    // Player health

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.isCancelled()) return;// Already canceled
        if (e.getEntity() instanceof Player && arena.hasPlayer((Player) e.getEntity())) {
            e.setCancelled(true);
            if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                Location spawn = arena.getSpawns().get(ThreadLocalRandom.current().nextInt(arena.getSpawns().size()));
                e.getEntity().teleport(spawn);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFood(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player && arena.hasPlayer((Player) e.getEntity())) {
            e.setCancelled(true);
        }
    }

    // Item interact

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true)
    public void onPickup(PlayerPickupItemEvent e) {
        if (arena.hasPlayer(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        if (arena.hasPlayer(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    // World interact

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (arena.hasPlayer(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (arena.hasPlayer(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (arena.hasPlayer(e.getPlayer())) {
            e.setCancelled(true);
            e.setBuild(false);
        }
    }
}
