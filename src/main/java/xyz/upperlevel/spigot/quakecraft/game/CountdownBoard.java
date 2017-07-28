package xyz.upperlevel.spigot.quakecraft.game;

import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;
import xyz.upperlevel.uppercore.board.Board;

import static java.lang.String.valueOf;

public class CountdownBoard extends Board {
    public CountdownBoard(CountdownPhase phase, Config config) {
        super(config);
        Game game = phase.getGame();
        getPlaceholders()
                .set("arena_id", game::getId)
                .set("arena_name", game::getName)
                .set("players", () -> valueOf(game.getPlayers().size()))
                .set("min_players", () -> valueOf(game.getMinPlayers()))
                .set("max_players", () -> valueOf(game.getMaxPlayers()))
                .set("countdown", () -> valueOf(phase.getTimer()));
    }

    public static CountdownBoard deserialize(CountdownPhase phase, Config config) {
        try {
            return new CountdownBoard(phase, config);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in waiting board");
            throw e;
        }
    }
}
