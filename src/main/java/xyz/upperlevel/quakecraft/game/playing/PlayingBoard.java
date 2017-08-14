package xyz.upperlevel.quakecraft.game.playing;

import xyz.upperlevel.quakecraft.game.GameBoard;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;

public class PlayingBoard extends GameBoard {
    public PlayingBoard(Config config) {
        super(null, config);
    }

    public PlayingBoard(PlayingPhase phase, PlayingBoard board) {
        super(phase.getParent(), board);
        getPlaceholders()
                .set("countdown", () -> phase.getTimer().toString("mm:ss"));
    }

    public static PlayingBoard deserialize(Config config) {
        try {
            return new PlayingBoard(config);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in playing board");
            throw e;
        }
    }
}