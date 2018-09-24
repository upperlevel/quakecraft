package xyz.upperlevel.quakecraft.arena;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.game.lobby.WaitingPhase;
import xyz.upperlevel.quakecraft.powerup.Powerup;
import xyz.upperlevel.uppercore.arena.Arena;
import xyz.upperlevel.uppercore.config.Config;
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
    @Setter
    private int minPlayers = -1, maxPlayers = -1;

    private List<Location> spawns = new ArrayList<>();
    private List<Powerup> powerups = new ArrayList<>();

    private final QuakeArenaListener listener;

    public QuakeArena(String id) {
        super(id);
        getPlaceholderRegistry()
                .set("arena_min_players", minPlayers)
                .set("arena_max_players", maxPlayers)
                .set("arena_spawns", spawns.size());

        // Register protection listener
        listener = new QuakeArenaListener(this);
        Bukkit.getPluginManager().registerEvents(listener, Quakecraft.get());
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
    public void start() {
        super.start();
        setPhase(new WaitingPhase());
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
                .map(Powerup::save)
                .collect(Collectors.toList()));
        return data;
    }

    @Override
    public void deserialize(Map<String, Object> data) {
        Config config = Config.from(data);
        minPlayers = config.getInt("min-players");
        maxPlayers = config.getInt("max-players");
        spawns = config.getLocationList("spawns");
        powerups = config.getConfigList("powerups", Collections.emptyList())
                .stream()
                .map(sub -> new Powerup(this, sub))
                .collect(Collectors.toList());
    }

    public static void loadConfig(Config config) {
        ARENA_JOIN_MESSAGE = config.getMessage("arena-join");
        ARENA_QUIT_MESSAGE = config.getMessage("arena-quit");

        MAX_PLAYERS_REACHED_ERROR = config.getMessage("cannot-join-max-reached");
    }
}
