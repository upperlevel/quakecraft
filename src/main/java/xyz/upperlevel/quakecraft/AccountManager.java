package xyz.upperlevel.quakecraft;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class AccountManager implements Listener {
    private final Map<Player, QuakeAccount> accounts = new HashMap<>();

    public AccountManager() {
        Bukkit.getOnlinePlayers().forEach(this::onJoin);
        Bukkit.getPluginManager().registerEvents(this, Quake.get());
    }

    private void onJoin(Player player) {
        QuakeAccount acc = new QuakeAccount(player);
        register(acc);
        Bukkit.getScheduler().runTaskAsynchronously(Quake.get(), acc::load);
    }

    private void onQuit(Player player) {
        QuakeAccount acc = getAccount(player);
        if (acc != null) {
            unregister(acc);
            Bukkit.getScheduler().runTaskAsynchronously(Quake.get(), acc::save);
        }
    }

    public void register(QuakeAccount account) {
        accounts.put(account.getPlayer(), account);
    }

    public QuakeAccount unregister(Player player) {
        return accounts.remove(player);
    }

    public QuakeAccount unregister(QuakeAccount player) {
        return accounts.remove(player.getPlayer());
    }

    public QuakeAccount getAccount(Player player) {
        return accounts.get(player);
    }

    public void close() {
        accounts.values().forEach(QuakeAccount::save);
        accounts.clear();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        onJoin(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        onQuit(e.getPlayer());
    }

    public static AccountManager get() {
        return Quake.get().getPlayerManager();
    }
}
