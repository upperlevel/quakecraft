package xyz.upperlevel.spigot.quakecraft.game;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GameManager {

    private List<Game> games = new ArrayList<>(); // to save
    private final Map<Arena, Game> gamesByArena = new HashMap<>(); // to optimize

    private final File file;

    public GameManager() {
        file = new File(QuakeCraftReloaded.get().getDataFolder(), "games.yml");
    }

    public void addGame(Game game) {
        games.add(game);
        gamesByArena.put(game.getArena(), game);
        game.start();
    }

    public Game removeGame(Arena arena) {
        Game game;
        if ((game = gamesByArena.remove(arena)) != null) {
            games.remove(game);
            game.stop();
        }
        return game;
    }

    public Game getGame(Arena arena) {
        return gamesByArena.get(arena);
    }

    public Game getGame(Player player) {
        for (Game game : games) {
            if (game.isPlaying(player))
                return game;
        }
        return null;
    }

    public List<Game> getGames() {
        return games;
    }

    public void load() {
        if (file.exists()) {
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            for (String arenaName : cfg.getStringList("games")) {
                Arena arena = QuakeCraftReloaded.get().getArenaManager().getArena(arenaName);
                if (arena != null)
                    addGame(new Game(arena));
            }
        }
    }

    public void save() throws IOException {
        file.getParentFile().mkdirs();
        file.createNewFile();

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        cfg.set("games", games.stream().map(Game::getId).collect(Collectors.toList()));
        cfg.save(file);
    }
}
