package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import xyz.upperlevel.spigot.quakecraft.game.play.PlayingPhase;
import xyz.upperlevel.uppercore.game.Phase;
import xyz.upperlevel.uppercore.game.PhaseManager;

import java.util.*;

import static xyz.upperlevel.uppercore.Uppercore.boards;

@Getter
public class GamePhase extends PhaseManager implements Phase, Listener {
    private final Game game;
    private final Map<Player, Participant> participants = new HashMap<>();
    private final List<Participant> ranking = new ArrayList<>();

    public GamePhase(Game game) {
        this.game = game;
    }

    private Participant register(Player player) {
        Participant participant = new Participant(this, player);
        participants.put(player, participant);
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
