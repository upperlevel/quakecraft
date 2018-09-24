package xyz.upperlevel.quakecraft.arena;

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
import xyz.upperlevel.uppercore.arena.events.ArenaJoinEvent;
import xyz.upperlevel.uppercore.arena.events.ArenaQuitEvent;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;

import static xyz.upperlevel.quakecraft.arena.QuakeArena.ARENA_JOIN_MESSAGE;
import static xyz.upperlevel.quakecraft.arena.QuakeArena.ARENA_QUIT_MESSAGE;
import static xyz.upperlevel.quakecraft.arena.QuakeArena.MAX_PLAYERS_REACHED_ERROR;

public class QuakeArenaListener implements Listener {

    private final QuakeArena arena;

    public QuakeArenaListener(QuakeArena arena) {
        this.arena = arena;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(ArenaJoinEvent e) {
        if (arena.equals(e.getArena())) {
            if (arena.getPlayers().size() > arena.getMaxPlayers()) {
                e.setCancelled(true);
                MAX_PLAYERS_REACHED_ERROR.send(e.getPlayer(), arena.getPlaceholderRegistry());
            } else {
                arena.getPlayers().forEach(player -> ARENA_JOIN_MESSAGE.send(e.getPlayer(), arena.getPlaceholderRegistry()));
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(ArenaQuitEvent e) {
        if (arena.equals(e.getArena())) {
            Player quit = e.getPlayer();
            arena.getPlayers().forEach(other -> ARENA_QUIT_MESSAGE.send(other,
                    PlaceholderRegistry.create(arena.getPlaceholderRegistry())
                            .set("player_name", quit.getName())
            ));
        }
    }

    // Player health

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && arena.hasPlayer((Player) e.getEntity())) {
            e.setCancelled(true);
            if (e.getCause() == EntityDamageEvent.DamageCause.VOID)
                ((Player) e.getEntity()).setHealth(0);
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
