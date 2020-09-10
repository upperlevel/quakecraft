package xyz.upperlevel.quakecraft.arena;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.phases.lobby.LobbyPhase;
import xyz.upperlevel.quakecraft.powerup.Powerup;
import xyz.upperlevel.uppercore.arena.Arena;
import xyz.upperlevel.uppercore.arena.Phase;
import xyz.upperlevel.uppercore.arena.event.ArenaQuitEvent;
import xyz.upperlevel.uppercore.arena.event.ArenaQuitEvent.ArenaQuitReason;
import xyz.upperlevel.uppercore.config.ConfigConstructor;
import xyz.upperlevel.uppercore.config.ConfigProperty;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.util.Dbg;
import xyz.upperlevel.uppercore.util.LocUtil;

import java.util.*;
import java.util.stream.Collectors;

public class QuakeArena extends Arena {
    @Getter
    @Setter
    private int minPlayers = -1, maxPlayers = -1;

    @Getter
    @Setter
    private int killsToWin;

    private List<Location> spawns = new ArrayList<>();
    private List<Powerup> powerups = new ArrayList<>();

    private QuakeArenaListener listener;

    public QuakeArena(String id) {
        super(id);
        // Register protection listener
        init();
    }

    @ConfigConstructor
    public QuakeArena(
            @ConfigProperty("id") String id,
            @ConfigProperty("lobby") Location lobby,
            @ConfigProperty("join-signs") List<Location> joinSigns,
            @ConfigProperty("spawns") List<Location> spawns,
            @ConfigProperty("powerups") List<Powerup> powerups,
            @ConfigProperty("min-players") int minPlayers,
            @ConfigProperty("max-players") int maxPlayers,
            @ConfigProperty("kills-to-win") Optional<Integer> killsToWin
            ) {
        super(id, lobby, joinSigns);
        this.spawns = spawns;
        this.powerups = powerups;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.killsToWin = killsToWin.orElse(20);
        init();
    }

    public PlaceholderRegistry createPlaceholders() {
        return super.createPlaceholders()
                .set("min_players", () -> Integer.toString(minPlayers))
                .set("max_players", () -> Integer.toString(maxPlayers));
    }

    private void init() {
        listener = new QuakeArenaListener(this);
        Bukkit.getPluginManager().registerEvents(listener, Quake.get());
    }

    public void addSpawn(Location spawn) {
        spawns.add(spawn);
    }

    public void removeSpawn(int which) {
        spawns.remove(which);
    }

    public List<Location> getSpawns() {
        return Collections.unmodifiableList(spawns);
    }

    public void addPowerup(Powerup powerup) {
        powerups.add(powerup);
    }

    public void removePowerup(int which) {
        powerups.remove(which);
    }

    public List<Powerup> getPowerups() {
        return Collections.unmodifiableList(powerups);
    }

    public void setLimits(int min, int max) {
        minPlayers = min;
        maxPlayers = max;
    }

    @Override
    public boolean isReady() {
        boolean res = super.isReady();
        return res && minPlayers > 0 && maxPlayers > 0 && spawns.size() > 0;
    }

    @Override
    public void decorate() {
        super.decorate();
    }

    @Override
    public void vacate() {
        super.vacate();
    }

    @Override
    public Phase getEntryPhase() {
        return new LobbyPhase(this);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            Bukkit.getPluginManager().registerEvents(listener, Quake.get());
        } else {
            HandlerList.unregisterAll(listener);
        }
    }

    @Override
    public boolean join(Player player) {
        boolean result = super.join(player);
        if (result) {
            // The on-join message is managed by below phases.
            // The player could be a spectator, and if it is, no-one should be notified.
            Dbg.pf("[%s] %s joined", getName(), player.getName());
        }
        return result;
    }

    @Override
    public boolean quit(Player player, ArenaQuitReason reason) {
        boolean result = super.quit(player, reason);
        if (result) {
            // The on-quit message is managed by below phases.
            // The player could be a spectator, and if it is, no-one should be notified.
            Dbg.pf("[%s] %s quit", getName(), player.getName());
        }
        return result;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = super.serialize();
        data.put("min-players", minPlayers);
        data.put("max-players", maxPlayers);
        data.put("spawns", spawns.stream()
                .map(LocUtil::serialize)
                .collect(Collectors.toList()));
        data.put("powerups", powerups.stream()
                .map(Powerup::serialize)
                .collect(Collectors.toList()));
        data.put("kills-to-win", killsToWin);
        return data;
    }
}
