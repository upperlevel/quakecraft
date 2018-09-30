package xyz.upperlevel.quakecraft.game.lobby;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.uppercore.arena.Phase;
import xyz.upperlevel.uppercore.arena.events.ArenaJoinEvent;
import xyz.upperlevel.uppercore.arena.events.ArenaQuitEvent;
import xyz.upperlevel.uppercore.board.Board;
import xyz.upperlevel.uppercore.board.BoardManager;
import xyz.upperlevel.uppercore.board.SimpleConfigBoard;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.hotbar.Hotbar;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.util.List;

public class WaitingPhase implements Phase, Listener {
    private static Hotbar hotbar;
    private static Board board;

    private static List<PlaceholderValue<String>> signLines;

    @Getter
    private final LobbyPhase lobbyPhase;

    @Getter
    private final QuakeArena arena;

    @Getter
    private final PlaceholderRegistry placeholderRegistry;

    public WaitingPhase(LobbyPhase lobbyPhase) {
        this.lobbyPhase = lobbyPhase;
        this.arena = lobbyPhase.getArena();
        this.placeholderRegistry = PlaceholderRegistry.create();
    }

    private void setupPlayer(Player player) {
        BoardManager.open(player, board, placeholderRegistry);
    }

    private void clearPlayer(Player player) {
        BoardManager.close(player);
    }

    private void updatePlayer(Player player) {
        BoardManager.update(player, placeholderRegistry);
    }

    private void updatePlayers() {
        arena.getPlayers().forEach(this::updatePlayer);
    }

    /**
     * Checks if the players count is higher than the min.
     */
    private void tryStartCountdown() {
        if (arena.getPlayers().size() >= arena.getMinPlayers()) {
            lobbyPhase.getPhaseManager().setPhase(new CountdownPhase(lobbyPhase));
        }
    }

    @Override
    public void onEnable(Phase previous) {
        Bukkit.getPluginManager().registerEvents(this, Quakecraft.get());
        arena.getPlayers().forEach(this::setupPlayer);
        tryStartCountdown();
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);
        arena.getPlayers().forEach(this::clearPlayer);
    }

    @EventHandler
    public void onArenaJoin(ArenaJoinEvent e) {
        if (arena.equals(e.getArena()) && arena.getPlayers().size() >= arena.getMaxPlayers()) {
            setupPlayer(e.getPlayer());
            tryStartCountdown();
            updatePlayers(); // Update other players' scoreboard that could contain players count
        }
    }

    @EventHandler
    public void onArenaQuit(ArenaQuitEvent e) {
        if (arena.equals(e.getArena())) {
            Player p = e.getPlayer();
            clearPlayer(p);
            updatePlayers();
        }
    }

    public static void loadConfig(Config config) {
        board = SimpleConfigBoard.create(config.getConfigRequired("board"));
        hotbar = config.getConfigRequired("hotbar").get(Hotbar.class, Quakecraft.get());
    }
}
