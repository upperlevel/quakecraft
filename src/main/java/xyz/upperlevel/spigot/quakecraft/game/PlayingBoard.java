package xyz.upperlevel.spigot.quakecraft.game;

import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;

public class PlayingBoard extends GameBoard {
    public PlayingBoard(PlayingPhase phase, Config config) {
        super(phase.getParent(), config);
        getPlaceholders()
                .set("countdown", () -> phase.getTimer().toString("mm:ss"));
    }

    public static PlayingBoard deserialize(PlayingPhase phase, Config config) {
        try {
            return new PlayingBoard(phase, config);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in playing board");
            throw e;
        }
    }
}