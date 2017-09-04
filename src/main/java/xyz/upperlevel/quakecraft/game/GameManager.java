package xyz.upperlevel.quakecraft.game;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.config.Config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class GameManager {
    private List<Game> games = new ArrayList<>();
    private final Map<String, Game> gamesById = new HashMap<>();

    private final File file;

    public GameManager() {
        file = new File(Quakecraft.get().getDataFolder(), "games.yml");
    }

    public void addGame(Game game) {
        games.add(game);
        gamesById.put(game.getArena().getId(), game);
        game.start();
    }

    public Game removeGame(Arena arena) {
        Game game = gamesById.remove(arena.getId());
        if(game != null) {
            games.remove(game);
            game.stop();
        }
        return game;
    }

    public Game getGame(String id) {
        return gamesById.get(id);
    }

    public Game getGame(Arena arena) {
        return gamesById.get(arena.getId());
    }

    public Game getGame(Player player) {
        for (Game game : games) {
            if (game.isPlaying(player))
                return game;
        }
        return null;
    }

    public void load() {
        if (file.exists()) {
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            for (Config game_cfg : Config.wrap(cfg).getConfigList("games")) {
                String arena_id = game_cfg.getString("id");
                Arena arena = Quakecraft.get().getArenaManager().getArena(arena_id);
                if (arena == null)
                    Quakecraft.get().getLogger().warning("Cannot find arena: " + arena_id);
                else
                    addGame(new Game(arena, game_cfg));
            }
        }
    }

    public void save() throws IOException {
        file.getParentFile().mkdirs();
        file.createNewFile();

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        cfg.set("games", games.stream()
                .map(Game::save)
                .collect(Collectors.toList()));
        cfg.save(file);
    }

    public void stop() {
        for (Game game : games)
            game.stop();
    }
}
