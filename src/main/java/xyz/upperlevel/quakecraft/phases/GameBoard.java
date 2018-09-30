package xyz.upperlevel.quakecraft.phases;

import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.uppercore.arena.Arena;
import xyz.upperlevel.uppercore.board.Board;
import xyz.upperlevel.uppercore.config.ConfigConstructor;
import xyz.upperlevel.uppercore.config.ConfigProperty;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameBoard implements Board {
    private final PlaceholderValue<String> title;
    private final List<PlaceholderValue<String>> header;

    private final PlaceholderValue<String> rankingLine;
    private final int rankingMaxSize;

    private final List<PlaceholderValue<String>> footer;

    @ConfigConstructor
    public GameBoard(
            @ConfigProperty("title") PlaceholderValue<String> title,
            @ConfigProperty("header") List<PlaceholderValue<String>> header,
            @ConfigProperty("ranking.line") PlaceholderValue<String> rankingLine,
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
        Arena arena = Quake.get().getArenaManager().getArena(player);
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
    public String getTitle(Player player, PlaceholderRegistry placeholders) {
        return title.resolve(player, placeholders);
    }

    @Override
    public List<String> getLines(Player player, PlaceholderRegistry placeholders) {
        List<String> lines = new ArrayList<>();
        lines.addAll(getHeader(player, placeholders));
        lines.addAll(getRanking(player, placeholders));
        lines.addAll(getFooter(player, placeholders));
        return lines;
    }

    @Override
    public int getAutoUpdateInterval() {
        return -1;
    }
}