package xyz.upperlevel.spigot.quakecraft.arena;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.uppercore.util.Config;
import xyz.upperlevel.uppercore.util.SerializationUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.ChatColor.GREEN;

@Data
public class Arena {

    private final String name;
    private String displayName;
    private int minPlayers = -1, maxPlayers = -1;
    private Location lobby;
    private List<Location> spawns = new ArrayList<>();

    private boolean enabled;

    public Arena(String name) {
        this.name = name.toLowerCase();
        displayName = name;
    }

    public void addSpawn(Location spawn) {
        spawns.add(spawn);
    }

    public void setLimits(int min, int max) {
        minPlayers = min;
        maxPlayers = max;
    }

    public boolean isReady() {
        return displayName != null && minPlayers > 0 && maxPlayers > 0 && lobby != null && spawns.size() > 0;
    }

    public Game getStartable() {
        return new Game(this);
    }

    public String toInfo() {
        String o = "";
        o += "§aName: " + name + "\n";
        o += "§aDisplay name: " + displayName + "\n";
        o += "§aMin players: " + minPlayers + "\n";
        o += "§aMax players: " + maxPlayers + "\n";
        o += "§aLobby: " + (lobby != null) + "\n";
        o += "§aSpawns: " + spawns.size() + "\n";
        return o;
    }

    public Map<String, Object> save() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("display-name", displayName);
        data.put("lobby", SerializationUtil.serialize(lobby));

        Map<String, Object> players = new HashMap<>();
        players.put("min", minPlayers);
        players.put("max", maxPlayers);
        data.put("players", players);

        List<Map<String, Object>> spawns = new ArrayList<>();
        for (Location spawn : this.spawns)
            spawns.add(SerializationUtil.serialize(spawn));
        data.put("spawns", spawns);
        return data;
    }

    @SuppressWarnings("unchecked")
    public static Arena load(Config config) {
        Arena arena = new Arena(config.getString("name"));
        arena.displayName = config.getString("display-name");
        arena.lobby = config.getLocation("lobby");

        Config players = config.getSection("players");
        arena.minPlayers = players.getInt("min");
        arena.maxPlayers = players.getInt("max");

        arena.spawns = config.getLocationList("spawns");
        return arena;
    }

    public static boolean isValidName(String name) {
        for (int i = 0; i < name.length(); i++)
            if (!Character.isAlphabetic(name.charAt(i)) && !Character.isDigit(name.charAt(i)))
                return false;
        return true;
    }
}
