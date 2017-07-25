package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Data;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;
import xyz.upperlevel.uppercore.board.Board;
import xyz.upperlevel.uppercore.board.BoardView;

import java.util.List;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.GREEN;

@Getter
public class GameBoard extends Board {
    private final GamePhase phase;

    public GameBoard(GamePhase phase, Config config) {
        super(config);
        this.phase = phase;
        add(new TextArea(config.getMessageList("header")));
        add(new Ranking(config.getConfigRequired("ranking")));
        add(new TextArea(config.getMessageList("footer")));
    }

    public static GameBoard deserialize(GamePhase phase, Config config) {
        try {
            return new GameBoard(phase, config);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in game board");
            throw e;
        }
    }

    // RANKING
    @Data
    private class Ranking implements Area {
        private final int size;
        private final PlaceholderValue<String> text;

        public Ranking(Config config) {
            try {
                size = config.getInt("size", -1);
                text = config.getMessageRequired("text");
            } catch (InvalidConfigurationException e) {
                e.addLocalizer("in ranking");
                throw e;
            }
        }

        @Override
        public void update() {
        }

        @Override
        public List<String> render(Player player, PlaceholderRegistry session) {
            return phase.getRanking().stream()
                    .map(p -> text.resolve(player, session))
                    .collect(Collectors.toList());
        }
    }
}
