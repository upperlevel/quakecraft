package xyz.upperlevel.quakecraft.arena;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import xyz.upperlevel.quakecraft.game.Game;
import xyz.upperlevel.quakecraft.powerup.Powerup;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.util.LocUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Getter
@Setter
public class Arena {

    private final String id;
    private String name;
    private int minPlayers = -1, maxPlayers = -1;
    private int killsToWin;
    private Location lobby;
    private List<Location> spawns = new ArrayList<>();
    private List<Powerup> powerups = new ArrayList<>();
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
        p.set("arena_item_boxes", () -> String.valueOf(getPowerups().size()));
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
        o += "&aPowerups: " + powerups.size() + "\n";
        return o;
    }

    public Map<String, Object> save() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("name", name);
        if(lobby != null)
            data.put("lobby", LocUtil.serialize(lobby));

        Map<String, Object> players = new HashMap<>();
        players.put("min", minPlayers);
        players.put("max", maxPlayers);
        data.put("players", players);

        List<Map<String, Object>> spawns = new ArrayList<>();
        for (Location spawn : this.spawns)
            spawns.add(LocUtil.serialize(spawn));
        data.put("spawns", spawns);

        data.put("kills_to_win", killsToWin);

        data.put("powerups", powerups.stream().map(Powerup::save).collect(Collectors.toList()));
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

        arena.spawns = config.getLocationList("spawns", emptyList());

        arena.killsToWin = config.getInt("kills_to_win", -1);

        arena.powerups = config.getConfigList("powerups", emptyList())
                .stream()
                .map(c -> new Powerup(arena, c))
                .collect(Collectors.toList());
        return arena;
    }

    public static boolean isValidName(String name) {
        for (int i = 0; i < name.length(); i++)
            if (!Character.isAlphabetic(name.charAt(i)) && !Character.isDigit(name.charAt(i)))
                return false;
        return true;
    }
}
