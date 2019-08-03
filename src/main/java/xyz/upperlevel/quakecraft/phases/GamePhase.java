package xyz.upperlevel.quakecraft.phases;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.quakecraft.shop.railgun.Railgun;
import xyz.upperlevel.uppercore.arena.Phase;
import xyz.upperlevel.uppercore.arena.PhaseManager;
import xyz.upperlevel.uppercore.arena.events.ArenaJoinEvent;
import xyz.upperlevel.uppercore.arena.events.ArenaQuitEvent;
import xyz.upperlevel.uppercore.board.BoardManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.message.Message;
import xyz.upperlevel.uppercore.sound.PlaySound;
import xyz.upperlevel.uppercore.task.Countdown;

import java.util.*;


public class GamePhase extends PhaseManager implements Phase, Listener {
    private static int gameCountdown = 0;
    private static GameBoard board;
    private static Message startMessage;
    private static PlaySound startSound;

    @Getter
    private final QuakeArena arena;

    private final Map<Player, Gamer> gamersByPlayer = new HashMap<>();
    private final List<Gamer> gamers = new ArrayList<>();

    private final Set<Player> spectators = new HashSet<>();

    @Getter
    private final PlaceholderRegistry<?> placeholderRegistry;

    @Getter
    private final Countdown countdown;

    public GamePhase(QuakeArena arena) {
        this.arena = arena;
        this.placeholderRegistry = PlaceholderRegistry.create(arena.getPlaceholderRegistry());
        buildPlaceholders();
        this.countdown = new Countdown(Quake.get(), gameCountdown * 20, 20,
                tick -> {
                    for (Player player : arena.getPlayers()) {
                        BoardManager.update(player, placeholderRegistry);
                    }
                },
                () -> {
                    // if there is at least one player left the winner is him
                    if (gamers.size() > 0) {
                        setPhase(new EndingPhase(this, gamers.get(0).getPlayer()));
                    } else {
                        // otherwise since we have no-one playing we can skip to LobbyPhase (should never reach this point)
                        arena.getPhaseManager().setPhase(new LobbyPhase(arena));
                    }
                }
        );
    }

    private void buildPlaceholders() {
        placeholderRegistry.set("ranking_name", (p, s) -> {
            if (s == null)
                return null;
            try {
                return gamers.get(Integer.parseInt(s) - 1).getPlayer().getName();
            } catch (Exception e) {
                return null;
            }
        });
        placeholderRegistry.set("ranking_kills", (p, s) -> {
            try {
                return String.valueOf(gamers.get(Integer.parseInt(s) - 1).getKills());
            } catch (Exception e) {
                return null;
            }
        });
        placeholderRegistry.set("ranking_gun", (p, s) -> {
            try {
                QuakeAccount player = Quake.get().getPlayerManager().getAccount(gamers.get(Integer.parseInt(s) - 1).getPlayer());
                return player.getGun() == null ? Railgun.CUSTOM_NAME.resolve(p) : player.getGun().getName().resolve(p);
            } catch (Exception e) {
                return null;
            }
        });
        placeholderRegistry.set("game_countdown", (p, s) -> countdown.toString());
    }

    /**
     * Sorts ranking to the player with the highest number of
     * kills to the one with lowest number of kills.
     */
    public void updateRanking() {
        gamers.sort((prev, next) -> (next.getKills() - prev.getKills()));
    }

    private void addGamer(Player player) {
        Gamer g = new Gamer(this, player);
        gamersByPlayer.put(player, g);
        gamers.add(g);

        // setup
        player.setGameMode(GameMode.ADVENTURE);

        if (startMessage != null) {
            startMessage.send(player);
        }
        if (startSound != null) {
            startSound.play(player);
        }
    }

    public boolean isGamer(Player player) {
        return gamersByPlayer.containsKey(player);
    }

    public Gamer getGamer(Player player) {
        return gamersByPlayer.get(player);
    }

    public List<Gamer> getGamers() {
        return Collections.unmodifiableList(gamers);
    }

    private void addSpectator(Player player) {
        spectators.add(player);

        // TODO setup spectator
        player.sendMessage("You're a spectator for the arena: " + arena.getName() + "!");
    }

    public boolean isSpectator(Player player) {
        return spectators.contains(player);
    }

    public Set<Player> getSpectators() {
        return Collections.unmodifiableSet(spectators);
    }

    /**
     * Gets all the players in the arena (spectators + gamers).
     */
    public List<Player> getPlayers() {
        return arena.getPlayers();
    }

    @Override
    public void onEnable(Phase previous) {
        Bukkit.getPluginManager().registerEvents(this, Quake.get());

        arena.getPlayers().forEach(this::addGamer);
        setPhase(new PlayingPhase(this));
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);

        setPhase(null);
    }

    @EventHandler
    public void onArenaJoin(ArenaJoinEvent e) {
        if (arena.equals(e.getArena())) {
            addSpectator(e.getPlayer());
        }
    }

    @EventHandler
    public void onArenaQuit(ArenaQuitEvent e) {
        if (arena.equals(e.getArena())) {
            Player p = e.getPlayer();
            Gamer g = gamersByPlayer.remove(p);
            gamers.remove(g);
            spectators.remove(p);

            if (gamers.size() == 0) {
                // if no gamer is left in the arena we can restart
                arena.getPhaseManager().setPhase(new LobbyPhase(arena));
            }
        }
    }

    public static void loadConfig() {
        Config config = Quake.getConfigSection("game");
        gameCountdown = config.getIntRequired("duration");
        board = config.getRequired("game-board", GameBoard.class);
        startMessage = config.getMessage("start-message");
        startSound = config.getPlaySound("start-sound");
    }
}
