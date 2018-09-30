package xyz.upperlevel.quakecraft;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class QuakeAccountManager implements Listener {

    private final Map<Player, QuakeAccount> players = new HashMap<>();

    public QuakeAccountManager() {
        Bukkit.getOnlinePlayers().forEach(this::onJoin);
        Bukkit.getPluginManager().registerEvents(this, Quake.get());
    }

    /**
     * Registers player and loads it async from the configured db.
     */
    private void onJoin(Player player) {
        QuakeAccount qp = new QuakeAccount(player);
        register(qp);
        Bukkit.getScheduler().runTaskAsynchronously(Quake.get(), qp::load);
    }

    /**
     * Unregisters the player and saves it async.
     */
    private void onQuit(Player player) {
        QuakeAccount qp = getPlayer(player);
        if (qp != null) {
            unregister(qp);
            Bukkit.getScheduler().runTaskAsynchronously(Quake.get(), qp::save);
        }
    }

    public void register(QuakeAccount player) {
        if(player != null) {
            players.put(player.getPlayer(), player);
        }
    }

    public QuakeAccount unregister(Player player) {
        return players.remove(player);
    }

    public QuakeAccount unregister(QuakeAccount player) {
        return players.remove(player.getPlayer());
    }

    public QuakeAccount getPlayer(Player player) {
        return players.get(player);
    }

    /**
     * Saves all the players sync and unregisters them all.
     */
    public void close() {
        players.values().forEach(QuakeAccount::save);
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

    public static QuakeAccountManager get() {
        return Quake.get().getPlayerManager();
    }
}
