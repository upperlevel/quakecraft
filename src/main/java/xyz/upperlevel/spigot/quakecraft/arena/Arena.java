package xyz.upperlevel.spigot.quakecraft.arena;

import lombok.Data;
import lombok.Getter;
import org.bukkit.Location;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.util.SerializationUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Arena {

    private final String id;
    private String name;
    private int minPlayers = -1, maxPlayers = -1;
    private int killsToWin;
    private Location lobby;
    private List<Location> spawns = new ArrayList<>();
    @Getter
    private PlaceholderRegistry placeholders;

    private boolean enabled;

    public Arena(String id) {
        this.id = id.toLowerCase();
        this.name = id;
        this.placeholders = PlaceholderRegistry.create();
        registerPlaceholders(placeholders);
    }

    public void addSpawn(Location spawn) {
        spawns.add(spawn);
    }

    public void setLimits(int min, int max) {
        minPlayers = min;
        maxPlayers = max;
    }

    public boolean isReady() {
        return name != null && minPlayers > 0 && maxPlayers > 0 && lobby != null && spawns.size() > 0 && killsToWin >= 0;
    }

    public Game getStartable() {
        return new Game(this);
    }

    public void registerPlaceholders(PlaceholderRegistry p) {
        p.set("arena", id);
        p.set("arena_name", this::getName);
        p.set("arena_min_players", () -> String.valueOf(getMinPlayers()));
        p.set("arena_max_players", () -> String.valueOf(getMaxPlayers()));
        p.set("arena_lobby", () -> String.valueOf(getLobby() != null));
        p.set("arena_spawns", () -> String.valueOf(getSpawns().size()));
        p.set("arena_kills_to_win", () -> String.valueOf(getKillsToWin()));
    }

    public String toInfo() {
        String o = "";
        o += "§aId: " + id + "\n";
        o += "§aName: " + name + "\n";
        o += "§aMin players: " + minPlayers + "\n";
        o += "§aMax players: " + maxPlayers + "\n";
        o += "§aLobby: " + (lobby != null) + "\n";
        o += "§aSpawns: " + spawns.size() + "\n";
        o += "&aKills to win:" + killsToWin + "\n";
        return o;
    }

    public Map<String, Object> save() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("name", name);
        if(lobby != null)
            data.put("lobby", SerializationUtil.serialize(lobby));

        Map<String, Object> players = new HashMap<>();
        players.put("min", minPlayers);
        players.put("max", maxPlayers);
        data.put("players", players);

        List<Map<String, Object>> spawns = new ArrayList<>();
        for (Location spawn : this.spawns)
            spawns.add(SerializationUtil.serialize(spawn));
        data.put("spawns", spawns);

        data.put("kills_to_win", killsToWin);
        return data;
    }

    @SuppressWarnings("unchecked")
    public static Arena load(Config config) {
        Arena arena = new Arena(config.getString("id"));
        arena.name = config.getString("name");
        arena.lobby = config.getLocation("lobby");

        Config players = config.getConfig("players");
        arena.minPlayers = players.getInt("min");
        arena.maxPlayers = players.getInt("max");

        arena.spawns = config.getLocationList("spawns");

        arena.killsToWin = config.getInt("kills_to_win", -1);
        return arena;
    }

    public static boolean isValidName(String name) {
        for (int i = 0; i < name.length(); i++)
            if (!Character.isAlphabetic(name.charAt(i)) && !Character.isDigit(name.charAt(i)))
                return false;
        return true;
    }
}
