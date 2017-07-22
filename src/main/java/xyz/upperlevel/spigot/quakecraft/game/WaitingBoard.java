package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Getter;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;
import xyz.upperlevel.uppercore.placeholder.PlaceholderSession;
import xyz.upperlevel.uppercore.scoreboard.Board;
import xyz.upperlevel.uppercore.scoreboard.BoardView;

@Getter
public class WaitingBoard extends Board {
    private final WaitingPhase phase;

    public WaitingBoard(WaitingPhase phase, Config config) {
        super(config);
        this.phase = phase;
    }

    @Override
    public void onUpdate(BoardView view) {
        Game g = phase.getGame();
        view.placeholders()
                .set("arena_id", g.getId())
                .set("arena_name", g.getName())
                .set("players", g.getPlayers().size())
                .set("min_players", g.getMinPlayers())
                .set("max_players", g.getMaxPlayers());
        view.update();
    }

    public static WaitingBoard deserialize(WaitingPhase phase, Config config) {
        try {
            return new WaitingBoard(phase, config);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in waiting scoreboard");
            throw e;
        }
    }
}
