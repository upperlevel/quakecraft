package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;
import xyz.upperlevel.uppercore.scoreboard.Board;
import xyz.upperlevel.uppercore.scoreboard.BoardView;

import java.util.List;

@Getter
public class GameBoard extends Board {

    private final List<String> header, footer;
    private PlaceholderValue<String> rankingLine;
    private int rankingSize;

    public GameBoard(Plugin plugin, String id, Config config) {
        super(plugin, id, config);
        header = config.getStringList("header");
        Config ranking = config.getConfig("ranking");
        if (ranking != null) {
            rankingLine = PlaceholderValue.stringValue(ranking.getString("line"));
            rankingSize = ranking.getInt("size");
        }
        footer = config.getStringList("footer");
    }

    public void setRanking(BoardView view, List<Participant> ranking) {

    }

    public static GameBoard deserialize(Plugin plugin, String id, Config config) {
        return new GameBoard(plugin, id, config);
    }
}
