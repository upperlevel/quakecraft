package xyz.upperlevel.quakecraft.phases;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.uppercore.arena.Phase;
import xyz.upperlevel.uppercore.arena.events.ArenaJoinEvent;
import xyz.upperlevel.uppercore.arena.events.ArenaQuitEvent;
import xyz.upperlevel.uppercore.board.Board;
import xyz.upperlevel.uppercore.board.BoardManager;
import xyz.upperlevel.uppercore.board.SimpleConfigBoard;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.message.Message;
import xyz.upperlevel.uppercore.sound.PlaySound;

import java.util.Map;

import static xyz.upperlevel.quakecraft.Quake.get;
import static xyz.upperlevel.uppercore.util.TypeUtil.typeOf;

public class CountdownPhase implements Phase, Listener {
    private static int countdownTimer;
    private static Map<String, Message> countdownMessages; // Each countdown second corresponds to a message
    private static Map<String, PlaySound> countdownSounds; // Each countdown second corresponds to a sound

    private static Board countdownBoard;

    @Getter
    private final LobbyPhase lobbyPhase;

    @Getter
    private final QuakeArena arena;

    @Getter
    private final PlaceholderRegistry placeholderRegistry;

    @Getter
    private Countdown countdown;

    public CountdownPhase(LobbyPhase lobbyPhase) {
        this.lobbyPhase = lobbyPhase;
        this.arena = lobbyPhase.getArena();
        this.placeholderRegistry = PlaceholderRegistry.create(arena.getPlaceholderRegistry())
                .set("countdown", () -> countdown.getTimer());
    }

    private void clearTick(Player player) {
        player.setLevel(0);
    }

    private void clearTick() {
        for (Player p : arena.getPlayers())
            clearTick(p);
    }

    private void setupPlayer(Player player) {
        BoardManager.open(player, countdownBoard, placeholderRegistry);
    }

    private void clearPlayer(Player player) {
        BoardManager.close(player);
        clearTick(player);
    }

    private void updateBoard(Player player) {
        BoardManager.update(player, placeholderRegistry);
    }

    private void updateBoards() {
        arena.getPlayers().forEach(this::updateBoard);
    }

    @Override
    public void onEnable(Phase previous) {
        Bukkit.getPluginManager().registerEvents(this, get());
        for (Player player : arena.getPlayers()) {
            BoardManager.open(player, countdownBoard, placeholderRegistry);
        }
        // Starts countdown
        countdown = new Countdown();
        countdown.runTaskTimer(Quake.get(), 0, 20);
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);
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
            Message message = countdownMessages.get(String.valueOf(timer));
            PlaySound sound = countdownSounds.get(String.valueOf(timer));

            for (Player player : arena.getPlayers()) {
                player.setLevel(timer);
                BoardManager.update(player, placeholderRegistry);
                if (message != null) {
                    message.send(player);
                }
                if (sound != null) {
                    sound.play(player);
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
        Config config = Quake.getConfigSection("game");
        countdownTimer = config.getIntRequired("countdown-timer");

        // Loads countdown messages: a message per each countdown second.
        countdownMessages = config.getRequired("countdown-messages", typeOf(Map.class, String.class, Message.class));

        // Loads countdown sounds: a sound per each countdown second.
        Config sounds = config.getRequired("countdown-sounds", typeOf(Map.class, String.class, PlaySound.class));

        countdownBoard = config.getRequired("countdown-board", SimpleConfigBoard.class);
    }
}
