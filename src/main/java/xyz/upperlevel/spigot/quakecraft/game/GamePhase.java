package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.spigot.quakecraft.core.PhaseManager;
import xyz.upperlevel.spigot.quakecraft.core.scoreboard.ScoreboardHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.ChatColor.*;

@Getter
public class GamePhase extends PhaseManager implements Phase, Listener {

    private final Game game;
    private final ScoreboardHandler scoreboard;

    private final Map<Player, GamePlayer> players = new HashMap<>();
    private final List<GamePlayer> ranking = new ArrayList<>();

    public GamePlayer getGamePlayer(Player player) {
        return players.get(player);
    }

    public GamePhase(Game game) {
        this.game = game;

        scoreboard = new ScoreboardHandler(YELLOW + "" + BOLD + "QUAKECRAFT");
        scoreboard.addBlankSpace();
        scoreboard.addLine(WHITE + "Time left: " + GREEN + "10:00");
        scoreboard.addBlankSpace();
        scoreboard.addBlankSpace();
        scoreboard.addBlankSpace();
        scoreboard.addBlankSpace();
        scoreboard.addBlankSpace();
        scoreboard.addBlankSpace();
        scoreboard.addBlankSpace();
        scoreboard.addBlankSpace();
        scoreboard.addBlankSpace();
        scoreboard.addBlankSpace();
        scoreboard.addBlankSpace();
        scoreboard.addBlankSpace();
        scoreboard.addBlankSpace();
    }

    private GamePlayer registerGamePlayer(Player p) {
        GamePlayer gamePl = new GamePlayer(p);
        players.put(p, new GamePlayer(p));
        ranking.add(gamePl);
        return gamePl;
    }

    public void setupPlayer(Player p) {
        p.setGameMode(GameMode.ADVENTURE);
        scoreboard.open(p);
    }

    public void updateRanking() {

        ranking.sort((o1, o2) -> (o1.getKills() - o2.getKills()));
    }

    public void updateScoreboard() {
        scoreboard.setLine(1, WHITE + "Time left: " + GREEN + "...");
        for (int i = 0; i < Math.min(ranking.size(), 11); i++)
            scoreboard.setLine(i + 3, GRAY + ranking.get(i).getPlayer().getName() + ": " + GREEN + ranking.get(i).getKills());
    }

    @Override
    public void onEnable(Phase previous) {
        for (Player p : game.getPlayers()) {
            setupPlayer(p);
            registerGamePlayer(p);
        }
        setPhase(new PlayPhase(this));
    }

    public GamePlayer getWinner() {
        return ranking.size() > 0 ? ranking.get(0) : null;
    }

    @Override
    public void onDisable(Phase next) {
        for (Player p : players.keySet())
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }
}
