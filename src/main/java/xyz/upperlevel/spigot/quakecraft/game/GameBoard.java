package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Data;
import lombok.Getter;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;
import xyz.upperlevel.uppercore.placeholder.PlaceholderSession;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;
import xyz.upperlevel.uppercore.scoreboard.Board;
import xyz.upperlevel.uppercore.scoreboard.BoardView;

import java.util.List;

@Getter
public class GameBoard extends Board {

    // RANKING
    @Data
    private static class Ranking {
        private PlaceholderValue<String> line;
        private int start, end, size;

        public Ranking(Config config) {
            line = config.getMessageRequired("line");
            start = config.getIntRequired("start");
            end = config.getIntRequired("end");
            size = end - start;
        }

        public static Ranking deserialize(Config config) {
            try {
                return new Ranking(config);
            } catch (InvalidConfigurationException e) {
                e.addLocalizer("in ranking");
                throw e;
            }
        }
    }

    private final GamePhase phase;
    private Ranking ranking;

    public GameBoard(GamePhase phase, Config config) {
        this.phase = phase;
        ranking = Ranking.deserialize(config);
    }

    @Override
    public void onUpdate(BoardView view) {
        // sets normal lines
        for (int position = 0; position < getLines().length; position++)
            view.setLine(position, getLine(position));
        // sets ranking
        List<Participant> participants = phase.getRanking();
        for (int position = 0; position < participants.size() && position < ranking.getSize(); position++)
            view.setLine(ranking.getStart() + position, ranking.getLine());
    }

    public static GameBoard deserialize(GamePhase phase, Config config) {
        try {
            return new GameBoard(phase, config);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in game scoreboard");
            throw e;
        }
    }
}
