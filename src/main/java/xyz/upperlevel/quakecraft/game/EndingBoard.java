package xyz.upperlevel.spigot.quakecraft.game;

import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;

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
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in ending phase");
            throw e;
        }
    }
}
