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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.spigot.quakecraft.core.PhaseManager;
import xyz.upperlevel.spigot.quakecraft.event.GameJoinEvent;
import xyz.upperlevel.spigot.quakecraft.event.GameQuitEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;

@Data
public class Game implements Listener {

    private final Arena arena;
    private final PhaseManager phaseManager = new PhaseManager();
    private final List<Player> players = new ArrayList<>();

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

    public void join(Player player) {
        players.add(player);
        GameJoinEvent e = new GameJoinEvent(this, player);
        Bukkit.getPluginManager().callEvent(e);
        if (e.isCancelled())
            players.remove(player);
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

    // --- EVENTS

    // no damage
    // void damage -> kill

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        e.setCancelled(true);

        if (!(e.getEntity() instanceof Player))
            return;

        if (!players.contains(e.getEntity()))
            return;

        if (e.getCause() == EntityDamageEvent.DamageCause.VOID)
            ((Player) e.getEntity()).setHealth(0);
    }

    // no pickup/drop items

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

    // an arena cannot be destroyed

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
}
