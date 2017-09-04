package xyz.upperlevel.quakecraft.arena;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.util.LocUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ArenaManager {

    @Getter
    private List<Arena> arenas = new ArrayList<>();
    private Map<String, Arena> arenasById = new HashMap<>();
    @Getter
    @Setter
    private Location lobby;

    @Getter
    private final File file;

    public ArenaManager() {
        file = new File(Quakecraft.get().getDataFolder(), "arenas.yml");
    }

    public void addArena(Arena arena) {
        arenas.add(arena);
        arenasById.put(arena.getId(), arena);
    }

    public Arena getArena(String id) {
        return arenasById.get(id.toLowerCase());
    }

    public Arena removeArena(String id) {
        Arena arena = arenasById.remove(id.toLowerCase());
        arenas.remove(arena);
        return arena;
    }

    public Map<String, Arena> getArenasById() {
        return Collections.unmodifiableMap(arenasById);
    }

    public void load() {
        if (file.exists()) {
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            if (cfg.contains("arenas")) {
                List<Map<?, ?>> data = cfg.getMapList("arenas");
                for (Map<?, ?> arena : data)
                    addArena(Arena.load(arena::get));
            }
            if(cfg.contains("lobby")) {
                lobby = LocUtil.deserialize(Config.wrap(cfg.getConfigurationSection("lobby")));
            }
        }
    }

    public void save() throws IOException {
        file.getParentFile().mkdirs();
        file.createNewFile();
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        List<Map<String, Object>> data = new ArrayList<>();
        for (Arena arena : arenas)
            data.add(arena.save());
        cfg.set("arenas", data);
        if(lobby != null)
            cfg.set("lobby", LocUtil.serialize(lobby));
        cfg.save(file);
    }
}
