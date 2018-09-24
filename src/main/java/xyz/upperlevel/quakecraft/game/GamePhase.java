package xyz.upperlevel.quakecraft.game;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import xyz.upperlevel.quakecraft.QuakePlayer;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.quakecraft.events.GameJoinEvent;
import xyz.upperlevel.quakecraft.events.GameQuitEvent;
import xyz.upperlevel.quakecraft.game.playing.PlayingPhase;
import xyz.upperlevel.quakecraft.powerup.Powerup;
import xyz.upperlevel.quakecraft.shop.railgun.Railgun;
import xyz.upperlevel.uppercore.arena.Phase;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;

import java.util.*;

import static xyz.upperlevel.quakecraft.Quakecraft.get;

@Getter
public class GamePhase extends PhaseManager implements Phase, Listener {
    public static Message CANNOT_JOIN_ALREADY_PLAYING;

    private final QuakeArena arena;
    private final Map<Player, Participant> participants = new HashMap<>();
    private final List<Participant> ranking = new ArrayList<>();
    private final PlaceholderRegistry<?> placeholders;

    public GamePhase(QuakeArena arena) {
        this.arena = arena;
        this.placeholders = PlaceholderRegistry.create(game.getPlaceholders());
        buildPlaceholders(placeholders);
    }


    public void buildPlaceholders(PlaceholderRegistry<?> reg) {
        reg.set("ranking_name", (p, s) -> {
            if (s == null)
                return null;
            try {
                return getRanking().get(Integer.parseInt(s) - 1).getPlayer().getName();
            } catch (Exception e) {
                return null;
            }
        });

        reg.set("ranking_kills", (p, s) -> {
            try {
                return String.valueOf(getRanking().get(Integer.parseInt(s) - 1).kills);
            } catch (Exception e) {
                return null;
            }
        });

        reg.set("ranking_gun", (p, s) -> {
            try {
                QuakePlayer player = get().getPlayerManager().getPlayer(getRanking().get(Integer.parseInt(s) - 1).getPlayer());
                return player.getGun() == null ? Railgun.CUSTOM_NAME.resolve(p) : player.getGun().getName().resolve(p);
            } catch (Exception e) {
                return null;
            }
        });
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

    public PlaceholderRegistry<?> getPlaceholders() {
        return placeholders;
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
