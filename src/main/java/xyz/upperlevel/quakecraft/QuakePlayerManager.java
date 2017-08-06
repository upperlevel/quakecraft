package xyz.upperlevel.quakecraft;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class QuakePlayerManager implements Listener {

    private final Map<Player, QuakePlayer> players = new HashMap<>();

    public QuakePlayerManager() {
        Bukkit.getOnlinePlayers().forEach(this::onJoin);
        Bukkit.getPluginManager().registerEvents(this, Quakecraft.get());
    }

    /**
     * Registers player and loads it async from the configured db.
     */
    private void onJoin(Player player) {
        QuakePlayer qp = new QuakePlayer(player);
        register(qp);
        Bukkit.getScheduler().runTaskAsynchronously(Quakecraft.get(), qp::load);
    }

    /**
     * Unregisters the player and saves it async.
     */
    private void onQuit(Player player) {
        QuakePlayer qp = getPlayer(player);
        if (qp != null) {
            unregister(qp);
            Bukkit.getScheduler().runTaskAsynchronously(Quakecraft.get(), qp::save);
        }
    }

    public void register(QuakePlayer player) {
        players.put(player.getPlayer(), player);
    }

    public QuakePlayer unregister(Player player) {
        return players.remove(player);
    }

    public QuakePlayer unregister(QuakePlayer player) {
        return players.remove(player.getPlayer());
    }

    public QuakePlayer getPlayer(Player player) {
        return players.get(player);
    }

    /**
     * Saves all the players sync and unregisters them all.
     */
    public void close() {
        players.values().forEach(QuakePlayer::save);
        players.clear();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        onJoin(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        onQuit(e.getPlayer());
    }

    public static QuakePlayerManager get() {
        return Quakecraft.get().getPlayerManager();
    }
}
