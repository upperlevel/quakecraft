package xyz.upperlevel.quakecraft.phases;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
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
import xyz.upperlevel.uppercore.sound.CompatibleSound;

import static java.lang.String.valueOf;
import static xyz.upperlevel.quakecraft.Quake.get;

public class CountdownPhase implements Phase, Listener {
    private static int COUNTDOWN_TIMER;
    private static Config COUNTDOWN_MESSAGES; // Each countdown second corresponds to a message

    private static Board COUNTDOWN_BOARD;
    private static Sound ORB_PICKUP = CompatibleSound.getRaw("ENTITY_EXPERIENCE_ORB_PICKUP");

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
        BoardManager.open(player, COUNTDOWN_BOARD, placeholderRegistry);
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
            BoardManager.open(player, COUNTDOWN_BOARD, placeholderRegistry);
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
            timer = COUNTDOWN_TIMER;
        }

        @Override
        public void run() {
            for (Player player : arena.getPlayers()) {
                player.setLevel(timer);
                player.playSound(player.getLocation(), ORB_PICKUP, 0, 100f);
                BoardManager.update(player, placeholderRegistry);

                Message msg = COUNTDOWN_MESSAGES.getMessage(valueOf(timer));
                if (msg != null) {
                    msg.send(player);
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

    public static void loadConfig(Config config) {
        COUNTDOWN_TIMER = config.getIntRequired("timer");
        COUNTDOWN_MESSAGES = config.getConfigRequired("messages");
        COUNTDOWN_BOARD = SimpleConfigBoard.create(config.getConfigRequired("board"));
    }
}
