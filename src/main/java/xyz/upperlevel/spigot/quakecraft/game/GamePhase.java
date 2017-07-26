package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.spigot.quakecraft.core.PhaseManager;
import xyz.upperlevel.spigot.quakecraft.core.PlayerUtil;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.board.BoardManager;

import java.io.File;
import java.util.*;

import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;
import static xyz.upperlevel.uppercore.Uppercore.boards;

@Getter
public class GamePhase extends PhaseManager implements Phase, Listener {

    private final Game game;

    private final Map<Player, Participant> participants = new HashMap<>();
    private final List<Participant> ranking = new ArrayList<>();

    private final GameBoard board;

    public GamePhase(Game game) {
        this.game = game;

        // custom load game board
        File file = new File(get().getBoards().getFolder(), "game_solo.yml");
        if (!file.exists()) {
            throw new IllegalArgumentException("Cannot find file: \"" + file.getPath() + "\"");
        }
        board = GameBoard.deserialize(this , Config.wrap(YamlConfiguration.loadConfiguration(file)));
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

    public Collection<Participant> getParticipants() {
        return participants.values();
    }

    private void setup(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        if (board != null)
            boards().view(player).setBoard(board);
    }

    private void clear(Player player) {
        boards().view(player).clear();
    }

    private void clear() {
        for (Player p : game.getPlayers())
            clear(p);
    }

    private void update(Player player) {
        boards().view(player).render();
    }

    private void update() {
        ranking.sort((o1, o2) -> (o1.getKills() - o2.getKills()));
    }

    public Participant getWinner() {
        return ranking.size() > 0 ? ranking.get(0) : null;
    }

    @Override
    public void onEnable(Phase previous) {
        for (Player player : game.getPlayers()) {
            register(player);
            setup(player);
        }
        setPhase(new PlayingPhase(this));
    }

    @Override
    public void onDisable(Phase next) {
        clear();
    }
}
