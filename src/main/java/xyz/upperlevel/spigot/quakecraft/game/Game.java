package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.spigot.quakecraft.core.PhaseManager;
import xyz.upperlevel.spigot.quakecraft.core.PlayerUtil;
import xyz.upperlevel.spigot.quakecraft.events.GameJoinEvent;
import xyz.upperlevel.spigot.quakecraft.events.GameQuitEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;

@Data
public class Game implements Listener {

    private final Arena arena;
    private final PhaseManager phaseManager = new PhaseManager();
    private final Set<Player> players = new HashSet<>();

    private Player winner;

    /**
     * Gets name of this arena.
     */
    public String getId() {
        return getArena().getName();
    }

    /**
     * Gets display name of this arena.
     */
    public String getName() {
        return getArena().getName();
    }

    /**
     * Gets minimum count of players of this arena.
     */
    public int getMinPlayers() {
        return getArena().getMinPlayers();
    }

    /**
     * Gets maximum count of players of this arena.
     */
    public int getMaxPlayers() {
        return getArena().getMaxPlayers();
    }

    public void start() {
        Bukkit.getPluginManager().registerEvents(this, get());
        getPhaseManager().setPhase(new LobbyPhase(this));
    }

    public void stop() {
        HandlerList.unregisterAll(this);
        getPhaseManager().setPhase(null);
    }

    public boolean join(Player player) {
        if (players.add(player)) {
            GameJoinEvent e = new GameJoinEvent(this, player);
            Bukkit.getPluginManager().callEvent(e);
            if (e.isCancelled())
                players.remove(player);
            return true;
        }
        return false;
    }

    public boolean isPlaying(Player player) {
        return players.contains(player);
    }

    public boolean leave(Player player) {
        if (players.remove(player)) {
            Bukkit.getPluginManager().callEvent(new GameQuitEvent(this, player));
            return true;
        }
        return false;
    }

    public void broadcast(String msg) {
        players.forEach(player -> player.sendMessage(msg));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (players.contains(e.getPlayer()))
            leave(e.getPlayer());
    }

    // PLAYER HEALTH

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && players.contains(e.getEntity())) {
            e.setCancelled(true);
            if (e.getCause() == EntityDamageEvent.DamageCause.VOID)
                ((Player) e.getEntity()).setHealth(0);
        }
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player && players.contains(e.getEntity()))
            e.setCancelled(true);
    }

    // ITEM INTERACT

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        if (players.contains(e.getPlayer()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (players.contains(e.getPlayer()))
            e.setCancelled(true);
    }

    // WORLD INTERACT

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (players.contains(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (players.contains(e.getPlayer()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (players.contains(e.getPlayer())) {
            e.setCancelled(true);
            e.setBuild(false);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (players.contains(e.getEntity())) {
            e.setKeepInventory(true);
            e.getEntity().spigot().respawn();
        }
    }
}
