package xyz.upperlevel.spigot.quakecraft.game;

import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;

public class EndingBoard extends GameBoard {
    public EndingBoard(EndingPhase phase, Config config) {
        super(phase.getParent(), config);
    }

    public static EndingBoard deserialize(EndingPhase phase, Config config) {
        try {
            return new EndingBoard(phase, config);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in ending phase");
            throw e;
        }
    }
}
