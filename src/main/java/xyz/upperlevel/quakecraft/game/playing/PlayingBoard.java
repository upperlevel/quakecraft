package xyz.upperlevel.quakecraft.game.playing;

import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.uppercore.board.Board;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.ConfigConstructor;
import xyz.upperlevel.uppercore.config.ConfigProperty;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlayingBoard implements Board {
    private final PlaceholderValue<String> title;
    private final List<PlaceholderValue<String>> header;
    private final List<PlaceholderValue<String>> footer;

    private final PlaceholderValue<String> rankingLine;
    private final int maxLines;

    @ConfigConstructor
    public PlayingBoard(
            @ConfigProperty(value = "title", optional = true) PlaceholderValue<String> title,
            @ConfigProperty(value = "header", optional = true) List<PlaceholderValue<String>> header,
            @ConfigProperty(value = "footer", optional = true) List<PlaceholderValue<String>> footer,
            @ConfigProperty(value = "ranking.line", optional = true) PlaceholderValue<String> rankingLine,
            @ConfigProperty("ranking.maxLines") Optional<Integer> maxLines
    ) {
        this.title = title;
        this.header = header;
        this.footer = footer;
        this.rankingLine = rankingLine;
        this.maxLines = maxLines.orElse(10);
    }

    @Override
    public String getTitle(Player player, PlaceholderRegistry placeholderRegistry) {
        return title.resolve(player, placeholderRegistry);
    }

    @Override
    public List<String> getLines(Player player, PlaceholderRegistry placeholderRegistry) {
        // header
        List<String> rendered = header.stream()
                .map(line -> line.resolve(player, placeholderRegistry))
                .collect(Collectors.toList());
        // body
        Quakecraft.get().getArenaManager().

        // footer
        rendered.addAll(footer.stream()
                .map(line -> line.resolve(player, placeholderRegistry))
                .collect(Collectors.toList()));
        return rendered;
    }

    @Override
    public int getAutoUpdateInterval() {
        return 0;
    }
}