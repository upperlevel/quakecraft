package xyz.upperlevel.spigot.quakecraft;

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
        Bukkit.getPluginManager().registerEvents(this, QuakeCraftReloaded.get());
    }

    public void register(QuakePlayer player) {
        players.put(player.getPlayer(), player);
    }

    public QuakePlayer getPlayer(Player player) {
        return players.get(player);
    }

    public QuakePlayer unregister(Player player) {
        return players.remove(player);
    }

    public QuakePlayer unregister(QuakePlayer player) {
        return players.remove(player.getPlayer());
    }

    public void registerAll() {
        Bukkit.getOnlinePlayers().forEach(p -> register(new QuakePlayer(p)));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        register(new QuakePlayer(e.getPlayer()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        unregister(e.getPlayer());
    }

    public static QuakePlayerManager get() {
        return QuakeCraftReloaded.get().getPlayerManager();
    }
}
