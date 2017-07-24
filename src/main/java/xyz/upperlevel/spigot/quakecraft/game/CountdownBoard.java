package xyz.upperlevel.spigot.quakecraft.game;

import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;
import xyz.upperlevel.uppercore.scoreboard.Board;
import xyz.upperlevel.uppercore.scoreboard.BoardView;

public class CountdownBoard extends Board {
    private final CountdownPhase phase;

    public CountdownBoard(CountdownPhase phase, Config config) {
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
                .set("max_players", g.getMaxPlayers())
                .set("countdown", phase.getTimer());
        view.update();
    }

    public static CountdownBoard deserialize(CountdownPhase phase, Config config) {
        try {
            return new CountdownBoard(phase, config);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in waiting scoreboard");
            throw e;
        }
    }
}
