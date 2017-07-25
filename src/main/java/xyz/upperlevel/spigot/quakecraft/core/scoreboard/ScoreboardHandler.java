package xyz.upperlevel.spigot.quakecraft.core.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.ChatColor.RESET;

/**
 * ScoreboardWrapper is a class that wraps Bukkit Board API
 * and makes your life easier.
 */
public class ScoreboardHandler {

    public static final int MAX_LINES = 15;

    private final Scoreboard handle;
    private final Objective objective;

    private final List<String> coded = new ArrayList<>(MAX_LINES);

    /**
     * Instantiates a new ScoreboardWrapper with a default title.
     */
    public ScoreboardHandler(String title) {
        handle = Bukkit.getScoreboardManager().getNewScoreboard();

        objective = handle.registerNewObjective(title, "dummy");
        objective.setDisplayName(title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    /**
     * Sets the handle title.
     */
    public void setTitle(String title) {
        objective.setDisplayName(title);
    }

    /**
     * Modifies the line with §r strings in the way to add
     * a line equal to another.
     */
    private String getLineCoded(String line) {
        String result = line;
        while (coded.contains(result))
            result += RESET;
        return result.substring(0, Math.min(40, result.length()));
    }

    /**
     * Adds a new line to the handle. Throw an error if the lines count are higher than 16.
     */
    public void addLine(String line) {
        if (coded.size() > MAX_LINES)
            throw new IndexOutOfBoundsException("You cannot add more than 16 lines.");
        String modified = getLineCoded(line);

        objective.getScore(modified).setScore(MAX_LINES - coded.size() + 1);

        coded.add(modified);
    }

    /**
     * Adds a blank space to the handle.
     */
    public void addBlankSpace() {
        addLine(" ");
    }

    /**
     * Sets a handle line to an exact index (between 0 and 15).
     */
    public void setLine(int index, String line) {
        if (index < 0 || index >= MAX_LINES)
            throw new IndexOutOfBoundsException("The index cannot be negative or higher than 15.");
        String oldModified = coded.get(index);

        handle.resetScores(oldModified);

        String modified = getLineCoded(line);
        coded.set(index, modified);

        objective.getScore(modified).setScore(MAX_LINES - index + 1);
    }

    /**
     * Gets the Bukkit Board.
     */
    public Scoreboard getHandle() {
        return handle;
    }

    /**
     * Opens this board to the given player.
     */
    public void open(Player player) {
        player.setScoreboard(handle);
    }

    /**
     * Remove handle opened to the given player.
     */
    public static void removeScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }
}
