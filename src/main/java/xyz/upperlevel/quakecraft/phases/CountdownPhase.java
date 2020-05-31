package xyz.upperlevel.quakecraft.phases;

import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.uppercore.arena.Phase;
import xyz.upperlevel.uppercore.arena.events.ArenaJoinEvent;
import xyz.upperlevel.uppercore.arena.events.ArenaQuitEvent;
import xyz.upperlevel.uppercore.board.BoardContainer;
import xyz.upperlevel.uppercore.board.BoardModel;
import xyz.upperlevel.uppercore.board.SimpleBoardModel;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.message.Message;

import java.util.Map;

import static xyz.upperlevel.uppercore.util.TypeUtil.typeOf;

public class CountdownPhase extends Phase {
    private static int countdownTimer;
    private static Map<Integer, Message> countdownMessages; // Each countdown second corresponds to a message
    private static Map<Integer, String> countdownSounds; // Each countdown second corresponds to a sound

    private static BoardModel countdownBoard;

    @Getter
    private final LobbyPhase lobbyPhase;

    @Getter
    private final QuakeArena arena;

    @Getter
    private final PlaceholderRegistry<?> placeholderRegistry;

    @Getter
    private Countdown countdown;

    private BoardContainer boards;

    public CountdownPhase(LobbyPhase lobbyPhase) {
        super("lobby-countdown");
        this.lobbyPhase = lobbyPhase;
        this.arena = lobbyPhase.getArena();
        this.placeholderRegistry = PlaceholderRegistry.create(arena.getPlaceholders())
                .set("countdown", () -> Integer.toString(countdown.getTimer()));
        this.boards = new BoardContainer(countdownBoard);
    }

    private void clearTick(Player player) {
        player.setLevel(0);
    }

    private void clearTick() {
        for (Player p : arena.getPlayers())
            clearTick(p);
    }

    private void setupPlayer(Player player) {
        boards.open(player, placeholderRegistry);
    }

    private void clearPlayer(Player player) {
        boards.close(player);
        clearTick(player);
    }

    private void updateBoard(Player player) {
        boards.update(player, placeholderRegistry);
    }

    private void updateBoards() {
        arena.getPlayers().forEach(this::updateBoard);
    }

    @Override
    public void onEnable(Phase previous) {
        super.onEnable(previous);
        arena.getPlayers().forEach(this::setupPlayer);
        // Starts countdown
        countdown = new Countdown();
        countdown.runTaskTimer(Quake.get(), 0, 20);
    }

    @Override
    public void onDisable(Phase next) {
        super.onDisable(next);
        countdown.cancel();
        arena.getPlayers().forEach(this::clearPlayer);
    }

    @EventHandler
    public void onArenaJoin(ArenaJoinEvent e) {
        if (arena.equals(e.getArena())) {
            setupPlayer(e.getPlayer());
            updateBoards();
        }
    }

    @EventHandler
    public void onArenaQuit(ArenaQuitEvent e) {
        if (arena.equals(e.getArena())) {
            clearPlayer(e.getPlayer());
            // If player count is lower than min stops the countdown if started
            if (arena.getPlayers().size() < arena.getMinPlayers()) {
                countdown.cancel();
                updateBoards();
                clearTick();
                lobbyPhase.getPhaseManager().setPhase(new WaitingPhase(lobbyPhase)); // Back to WaitingPhase
            }
        }
    }

    public class Countdown extends BukkitRunnable {
        @Getter
        private int timer;

        public Countdown() {
            timer = countdownTimer;
        }

        @Override
        public void run() {
            // Each second tries to get a message and a sound. If they have been
            // found in configuration, they are printed foreach player.
            Message message = countdownMessages.get(timer);
            Sound sound = Sound.valueOf(countdownSounds.get(timer));

            for (Player player : arena.getPlayers()) {
                player.setLevel(timer);
                updateBoard(player);
                if (message != null) {
                    message.send(player);
                }
                if (sound != null) {
                    player.playSound(player.getLocation(), sound, 100.0f, 100.0f);
                }
            }
            // updateSigns();
            if (timer > 0)
                timer--;
            else {
                cancel();
                arena.getPhaseManager().setPhase(new GamePhase(arena));
            }
        }
    }

    public static void loadConfig() {
        Config config = Quake.getConfigSection("lobby");
        countdownTimer = config.getIntRequired("countdown-timer");

        // Loads countdown messages: a message per each countdown second.
        countdownMessages = config.getRequired("countdown-messages", typeOf(Map.class, Integer.class, Message.class));

        // Loads countdown sounds: a sound per each countdown second.
        countdownSounds = config.getRequired("countdown-sounds", typeOf(Map.class, Integer.class, String.class));

        countdownBoard = config.getRequired("countdown-board", SimpleBoardModel.class);
    }
}
