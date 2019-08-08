package xyz.upperlevel.quakecraft.phases;

import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.uppercore.arena.Arena;
import xyz.upperlevel.uppercore.board.Board;
import xyz.upperlevel.uppercore.board.BoardModel;
import xyz.upperlevel.uppercore.config.ConfigConstructor;
import xyz.upperlevel.uppercore.config.ConfigProperty;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameBoard implements BoardModel {
    private final PlaceholderValue<String> title;
    private final List<PlaceholderValue<String>> header;

    private final PlaceholderValue<String> rankingLine;
    private final int rankingMaxSize;

    private final List<PlaceholderValue<String>> footer;

    @ConfigConstructor
    public GameBoard(
            @ConfigProperty("title") PlaceholderValue<String> title,
            @ConfigProperty("header") List<PlaceholderValue<String>> header,
            @ConfigProperty("ranking.text") PlaceholderValue<String> rankingLine,
            @ConfigProperty("ranking.max-size") int rankingMaxSize,
            @ConfigProperty("footer") List<PlaceholderValue<String>> footer
    ) {
        this.title = title;
        this.header = header;
        this.rankingLine = rankingLine;
        this.rankingMaxSize = rankingMaxSize;
        this.footer = footer;
    }

    private List<String> getHeader(Player player, PlaceholderRegistry placeholders) {
        return header.stream()
                .map(line -> line.resolve(player, placeholders))
                .collect(Collectors.toList());
    }

    private List<String> getRanking(Player player, PlaceholderRegistry placeholders) { // TODO inherit PlaceholderRegistry
        Arena arena = Quake.getArena(player);
        Stream<String> ranking = ((GamePhase) arena.getPhaseManager().getPhase()).getGamers()
                .stream()
                .map(gamer -> rankingLine.resolve(player, PlaceholderRegistry.wrap(
                        "player_name", gamer.getName(),
                        "kills", String.valueOf(gamer.getKills())
                )));
        if (rankingMaxSize >= 0) {
            ranking = ranking.limit(rankingMaxSize);
        }
        return ranking.collect(Collectors.toList());
    }

    private List<String> getFooter(Player player, PlaceholderRegistry placeholders) {
        return footer.stream()
                .map(line -> line.resolve(player, placeholders))
                .collect(Collectors.toList());
    }

    @Override
    public void apply(Board board, Player player) {
        GamePhase phase = (GamePhase) Quake.get().getArenaManager().get(player).getPhaseManager().getPhase();
        PlaceholderRegistry placeholders = phase.getPlaceholderRegistry();

        board.setTitle(title);
        board.setLines(new ArrayList<String>() {{
            addAll(getHeader(player, placeholders));
            addAll(getRanking(player, placeholders));
            addAll(getFooter(player, placeholders));
        }});
    }
}