package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;
import xyz.upperlevel.uppercore.board.Board;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class GameBoard extends Board {
    private final GamePhase phase;
    private TextArea header, footer;
    private Ranking ranking;

    public GameBoard(Config config) {
        this(null, config);
    }

    public GameBoard(GamePhase phase, Config config) {
        super(config);
        this.phase = phase;
        this.header = new TextArea(config.getMessageStrList("header"));
        this.ranking = new Ranking(config.getConfigRequired("ranking"));
        this.footer = new TextArea(config.getMessageStrList("footer"));
    }

    public GameBoard(GamePhase phase, GameBoard sample) {
        super(sample);
        this.phase = phase;
        this.header = sample.header;
        this.ranking = new Ranking(sample.ranking);
        this.footer = sample.footer;
        add(header);
        add(ranking);
        add(footer);
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
                text = config.getMessageStrRequired("text");
            } catch (InvalidConfigurationException e) {
                e.addLocalizer("in ranking");
                throw e;
            }
        }

        public Ranking(Ranking sample) {
            this.size = sample.size;
            this.text = sample.text;
        }

        @Override
        public void update() {
        }

        @Override
        public List<String> render(Player player, PlaceholderRegistry placeholders) {
            Stream<String> stream = phase.getRanking().stream()
                    .map(participant ->
                        text.resolve(
                                player,
                                PlaceholderRegistry.wrap(
                                        "player_name", participant.getName(),
                                        "kills", String.valueOf(participant.getKills())
                                )
                        )
                    );
            if (size >= 0)
                stream = stream.limit(size);
            return stream.collect(Collectors.toList());
        }

        @Override
        public Ranking copy() {
            return this;
        }
    }
}
