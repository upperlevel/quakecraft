package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Getter;
import xyz.upperlevel.uppercore.board.Board;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;

import static java.lang.String.valueOf;

@Getter
public class WaitingBoard extends Board {
    public WaitingBoard(Config config) {
        super(config);
    }

    public WaitingBoard(WaitingPhase phase, WaitingBoard board) {
        super(board);
        Game game = phase.getGame();
        getPlaceholders()
                .set("arena_id", game::getId)
                .set("arena_name", game::getName)
                .set("players", () -> valueOf(game.getPlayers().size()))
                .set("min_players",() -> valueOf(game.getMinPlayers()))
                .set("max_players", () -> valueOf(game.getMaxPlayers()));
    }

    public static WaitingBoard deserialize(Config config) {
        try {
            return new WaitingBoard(config);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in waiting board");
            throw e;
        }
    }
}
