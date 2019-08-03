package xyz.upperlevel.quakecraft.arena;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.phases.LobbyPhase;
import xyz.upperlevel.quakecraft.powerup.Powerup;
import xyz.upperlevel.uppercore.arena.Arena;
import xyz.upperlevel.uppercore.arena.Phase;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.ConfigConstructor;
import xyz.upperlevel.uppercore.config.ConfigProperty;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.message.Message;
import xyz.upperlevel.uppercore.util.LocUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QuakeArena extends Arena {
    public static Message ARENA_JOIN_MESSAGE;
    public static Message ARENA_QUIT_MESSAGE;
    public static Message MAX_PLAYERS_REACHED_ERROR;

    @Getter
    private String name;

    @Getter
    @Setter
    private int minPlayers = -1, maxPlayers = -1;

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
            @ConfigProperty("name") String name,
            @ConfigProperty("lobby") Location lobby,
            @ConfigProperty("spawns") List<Location> spawns,
            @ConfigProperty("powerups") List<Powerup> powerups
    ) {
        super(id, lobby);
        this.name = name;
        this.spawns = spawns;
        this.powerups = powerups;
        init();
    }

    private void init() {
        listener = new QuakeArenaListener(this);
        Bukkit.getPluginManager().registerEvents(listener, Quake.get());
    }

    public PlaceholderRegistry getPlaceholderRegistry() {
        return PlaceholderRegistry.def();
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
        if (getPlayers().size() > maxPlayers) {
            MAX_PLAYERS_REACHED_ERROR.send(player, PlaceholderRegistry.def());
            return false;
        } else {
            getPlayers().forEach(mate -> ARENA_JOIN_MESSAGE.send(player, PlaceholderRegistry.def()));
            return super.join(player);
        }
    }

    @Override
    public void quit(Player player) {
        getPlayers().forEach(other -> ARENA_QUIT_MESSAGE.send(player,
                PlaceholderRegistry.create()
                        .set("player_name", player.getName())
        ));
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
        return data;
    }

    public static void loadConfig(Config config) {
        ARENA_JOIN_MESSAGE = config.getMessage("arena-join");
        ARENA_QUIT_MESSAGE = config.getMessage("arena-quit");

        MAX_PLAYERS_REACHED_ERROR = config.getMessage("cannot-join-max-reached");
    }
}
