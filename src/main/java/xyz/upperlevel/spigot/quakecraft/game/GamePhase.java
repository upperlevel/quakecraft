package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.spigot.quakecraft.core.PhaseManager;
import xyz.upperlevel.uppercore.scoreboard.Board;
import xyz.upperlevel.uppercore.scoreboard.ScoreboardSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;

@Getter
public class GamePhase extends PhaseManager implements Phase, Listener {

    private final Game game;

    private final Map<Player, Participant> participants = new HashMap<>();
    private final List<Participant> ranking = new ArrayList<>();

    private static final GameBoard board;
    static {
        // custom load game scoreboard
        File file = new File(get().getScoreboards().getFolder(), "ingame_solo.yml");
        if (!file.exists()) {
            throw new IllegalArgumentException("Cannot find file: \"" + file.getPath() + "\"");
        }
        board = GameBoard.deserialize(get() , "ingame_solo", YamlConfiguration.loadConfiguration(file)::get);
        get().getScoreboards().register(board);
    }

    public GamePhase(Game game) {
        this.game = game;
    }

    private Participant register(Player player) {
        Participant participant = new Participant(player);
        participants.put(player, new Participant(player));
        ranking.add(participant);
        return participant;
    }

    public Participant getParticipant(Player player) {
        return participants.get(player);
    }

    private void setup(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        Board board = get().getScoreboards().get("ingame_solo");
        if (board != null)
            ScoreboardSystem.view(player).setBoard(board);
    }

    private void update(Player player) {
        ScoreboardSystem.view(player).update();
    }

    private void update() {
        ranking.sort((o1, o2) -> (o1.getKills() - o2.getKills()));
    }

    @Override
    public void onEnable(Phase previous) {
        for (Player player : game.getPlayers()) {
            register(player);
            setup(player);
        }
        setPhase(new PlayingPhase(this));
    }

    public Participant getWinner() {
        return ranking.size() > 0 ? ranking.get(0) : null;
    }

    @Override
    public void onDisable(Phase next) {
        for (Player player : game.getPlayers())
            ScoreboardSystem.view(player).clear();
    }
}
