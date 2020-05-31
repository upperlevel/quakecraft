package xyz.upperlevel.quakecraft.phases;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.uppercore.arena.Phase;
import xyz.upperlevel.uppercore.arena.events.ArenaJoinEvent;
import xyz.upperlevel.uppercore.arena.events.ArenaQuitEvent;
import xyz.upperlevel.uppercore.board.BoardContainer;
import xyz.upperlevel.uppercore.board.BoardModel;
import xyz.upperlevel.uppercore.board.SimpleBoardModel;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;

import java.util.List;
import java.util.stream.Collectors;

public class WaitingPhase extends Phase {
    private static BoardModel board;

    private final BoardContainer boards;

    @Getter
    private final LobbyPhase lobbyPhase;

    @Getter
    private final QuakeArena arena;

    @Getter
    private final PlaceholderRegistry placeholderRegistry;

    public WaitingPhase(LobbyPhase lobbyPhase) {
        super("waiting");

        this.boards = new BoardContainer(board);

        this.lobbyPhase = lobbyPhase;
        this.arena = lobbyPhase.getArena();
        this.placeholderRegistry = arena.getPlaceholders();
    }

    private void setupPlayer(Player player) {
        boards.open(player, placeholderRegistry);
    }

    private void clearPlayer(Player player) {
        boards.close(player);
    }

    private void tryStartCountdown() {
        if (arena.getPlayers().size() >= arena.getMinPlayers()) {
            lobbyPhase.getPhaseManager().setPhase(new CountdownPhase(lobbyPhase));
        }
    }

    @Override
    public void onEnable(Phase previous) {
        super.onEnable(previous);
        arena.getPlayers().forEach(this::setupPlayer);
        tryStartCountdown();
    }

    @Override
    public void onDisable(Phase next) {
        super.onDisable(next);
        arena.getPlayers().forEach(this::clearPlayer);
    }

    @EventHandler
    public void onArenaJoin(ArenaJoinEvent event) {
        if (arena.equals(event.getArena())) {
            Player player = event.getPlayer();
            setupPlayer(player);
            player.teleport(arena.getLobby());
            tryStartCountdown();

            // ArenaJoinEvent is called before the player's actually join (to permit cancellation).
            // For this reason, the board update is called the next tick.
            Bukkit.getScheduler().runTaskLater(Quake.get(), () ->
                    arena.getPlayers().forEach(in -> boards.update(in, placeholderRegistry)), 1);
        }
    }

    @EventHandler
    public void onArenaQuit(ArenaQuitEvent e) {
        if (arena.equals(e.getArena())) {
            Player p = e.getPlayer();
            boards.close(p);

            // ArenaQuitEvent is called before the player's actually quit (to permit cancellation).
            // For this reason, the board update is called the next tick.
            Bukkit.getScheduler().runTaskLater(Quake.get(), () ->
                    arena.getPlayers().forEach(in -> boards.update(in, placeholderRegistry)), 1);
        }
    }

    public static void loadConfig() {
        board = Quake.getConfigSection("lobby.waiting-board").get(SimpleBoardModel.class);
    }
}
