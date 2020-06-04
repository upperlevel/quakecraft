package xyz.upperlevel.quakecraft.phases.game;

import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.uppercore.board.Board;
import xyz.upperlevel.uppercore.board.BoardModel;
import xyz.upperlevel.uppercore.config.ConfigConstructor;
import xyz.upperlevel.uppercore.config.ConfigProperty;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public GamePhase getGamePhase(Player player) {
        return (GamePhase) Quake.get().getArenaManager().get(player).getPhaseManager().getPhase();
    }

    private List<String> resolveHeader(Player player, PlaceholderRegistry<?> placeholders) {
        return header
                .stream()
                .map(line -> line.resolve(player, placeholders))
                .collect(Collectors.toList());
    }

    private List<String> resolveRanking(Player player, PlaceholderRegistry<?> placeholders) {
        return getGamePhase(player).getGamers()
                .stream()
                .limit(rankingMaxSize)
                .map(gamer -> rankingLine.resolve(
                        player,
                        PlaceholderRegistry.create(placeholders)
                                .set("player_name", gamer.getName())
                                .set("kills", String.valueOf(gamer.getKills()))
                ))
                .collect(Collectors.toList());
    }

    private List<String> resolveFooter(Player player, PlaceholderRegistry<?> placeholders) {
        return footer
                .stream()
                .map(line -> line.resolve(player, placeholders))
                .collect(Collectors.toList());
    }

    public List<String> resolveLines(Player player, PlaceholderRegistry<?> placeholders) {
        return new ArrayList<String>() {{
            addAll(resolveHeader(player, placeholders));
            addAll(resolveRanking(player, placeholders));
            addAll(resolveFooter(player, placeholders));
        }};
    }


    @Override
    public void apply(Board board, Player player, PlaceholderRegistry<?> placeholders) {
        String resTitle = title.resolve(player, placeholders);
        board.setTitle(resTitle);
        //Dbg.pf("[GameBoard] Updating for %s - title: %s", player.getName(), resTitle);

        List<String> resLines = resolveLines(player, placeholders); // Every second instantiates a new list for players (care about GC perf?).
        board.setLines(resLines);
        //Dbg.pf("[GameBoard] Updating for %s - lines: %d", player.getName(), resLines.size());
    }
}