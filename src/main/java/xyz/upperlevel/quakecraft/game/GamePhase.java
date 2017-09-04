package xyz.upperlevel.quakecraft.game;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.events.GameJoinEvent;
import xyz.upperlevel.quakecraft.events.GameQuitEvent;
import xyz.upperlevel.quakecraft.game.playing.PlayingPhase;
import xyz.upperlevel.quakecraft.powerup.Powerup;
import xyz.upperlevel.uppercore.game.Phase;
import xyz.upperlevel.uppercore.game.PhaseManager;
import xyz.upperlevel.uppercore.message.Message;

import java.util.*;

import static xyz.upperlevel.uppercore.Uppercore.boards;

@Getter
public class GamePhase extends PhaseManager implements QuakePhase, Listener {
    public static Message CANNOT_JOIN_ALREADY_PLAYING;

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
        Bukkit.getPluginManager().registerEvents(this, Quakecraft.get());
        for (Player player : game.getPlayers()) {
            register(player);
            setup(player);
        }
        setPhase(new PlayingPhase(this));
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);
        setPhase(null);
        clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerJoin(GameJoinEvent event) {
        if(event.getGame() == getGame()) {
            event.cancel(CANNOT_JOIN_ALREADY_PLAYING.get(event.getPlayer(), getGame().getPlaceholders()));
        }
    }

    @EventHandler
    public void onGameQuit(GameQuitEvent event) {
        if(event.getGame() == game) {
            Participant p = participants.remove(event.getPlayer());
            ranking.remove(p);
            if(getPhase() instanceof PlayingPhase) {
                //Clear powerup effects
                getGame().getArena().getPowerups()
                        .stream()
                        .map(Powerup::getEffect)
                        .distinct()
                        .forEach(e -> e.clear(Collections.singletonList(p)));
            }
        }
    }

    public static void loadConfig() {
        CANNOT_JOIN_ALREADY_PLAYING = Quakecraft.get().getMessages().get("game.cannot-join.in-game");
    }

    @Override
    public void updateSigns() {
        ((QuakePhase) getPhase()).updateSigns();
    }
}