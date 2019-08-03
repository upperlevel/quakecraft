package xyz.upperlevel.quakecraft.phases;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
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

public class WaitingPhase implements Phase, Listener {
    private static Board board;

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

    private void tryStartCountdown() {
        if (arena.getPlayers().size() >= arena.getMinPlayers()) {
            lobbyPhase.getPhaseManager().setPhase(new CountdownPhase(lobbyPhase));
        }
    }

    @Override
    public void onEnable(Phase previous) {
        Bukkit.getPluginManager().registerEvents(this, Quake.get());
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
            updatePlayers(); // update other players' scoreboard that could contain players count
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

    public static void loadConfig() {
        board = SimpleConfigBoard.create(Quake.getConfigSection("game.waiting-board"));
    }
}
