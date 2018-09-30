package xyz.upperlevel.quakecraft.game.ending;

import xyz.upperlevel.quakecraft.game.GameBoard;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigException;

public class EndingBoard extends GameBoard {
    public EndingBoard(Config config) {
        super(config);
    }

    public EndingBoard(EndingPhase phase, EndingBoard sample) {
        super(phase.getParent(), sample);
    }

    public static EndingBoard deserialize(Config config) {
        try {
            return new EndingBoard(config);
        } catch (InvalidConfigException e) {
            e.addLocation("in ending phase");
            throw e;
        }
    }
}
