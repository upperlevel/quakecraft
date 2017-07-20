package xyz.upperlevel.spigot.quakecraft.arena;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArenaManager {

    @Getter
    private List<Arena> arenas = new ArrayList<>();
    private final Map<String, Arena> arenasByName = new HashMap<>();

    @Getter
    private final File file;

    public ArenaManager() {
        file = new File(QuakeCraftReloaded.get().getDataFolder(), "arenas.yml");
    }

    public void addArena(Arena arena) {
        arenas.add(arena);
        arenasByName.put(arena.getId(), arena);
    }

    public Arena getArena(String name) {
        return arenasByName.get(name.toLowerCase());
    }

    public Arena removeArena(String name) {
        Arena arena = arenasByName.remove(name.toLowerCase());
        arenas.remove(arena);
        return arena;
    }

    public void load() {
        if (file.exists()) {
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            if (cfg.contains("arenas")) {
                List<Map<?, ?>> data = cfg.getMapList("arenas");
                for (Map<?, ?> arena : data)
                    addArena(Arena.load(arena::get));
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
        cfg.save(file);
    }
}
