package xyz.upperlevel.quakecraft.phases.lobby;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.uppercore.arena.Phase;
import xyz.upperlevel.uppercore.arena.event.ArenaJoinEvent;
import xyz.upperlevel.uppercore.arena.event.ArenaQuitEvent;
import xyz.upperlevel.uppercore.arena.event.JoinSignUpdateEvent;
import xyz.upperlevel.uppercore.board.Board;
import xyz.upperlevel.uppercore.board.BoardModel;
import xyz.upperlevel.uppercore.board.SimpleBoardModel;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.message.Message;

import java.util.HashMap;
import java.util.Map;

public class WaitingPhase extends Phase {
    private static BoardModel board;
    private static Message joinSign;

    @Getter
    private final LobbyPhase lobbyPhase;

    @Getter
    private final QuakeArena arena;

    @Getter
    private final PlaceholderRegistry<?> placeholders;

    private final Map<Player, BoardModel.Hook> boardByPlayer = new HashMap<>();

    public WaitingPhase(LobbyPhase lobbyPhase) {
        super("waiting");

        this.lobbyPhase = lobbyPhase;
        this.arena = lobbyPhase.getArena();
        this.placeholders = arena.getPlaceholders();
    }

    private void setupPlayer(Player player) {
        BoardModel.Hook hooked = board.hook(new Board());
        boardByPlayer.put(player, hooked);
        hooked.open(player, placeholders);
    }

    private void clearPlayer(Player player) {
        boardByPlayer.remove(player);
    }

    private void tryStartCountdown() {
        if (arena.getPlayers().size() >= arena.getMinPlayers()) {
            lobbyPhase.setPhase(new CountdownPhase(lobbyPhase));
        }
    }

    @Override
    public void onEnable(Phase previous) {
        super.onEnable(previous);
        arena.getPlayers().forEach(this::setupPlayer);
        arena.updateJoinSigns();

        tryStartCountdown();
    }

    @EventHandler
    public void onArenaJoin(ArenaJoinEvent event) {
        if (arena.equals(event.getArena())) {
            Player player = event.getPlayer();
            setupPlayer(player);

            // ArenaJoinEvent is called before the player's actually join (to permit cancellation).
            // For this reason, the code that has to read the new arena's players is run one tick later.
            Bukkit.getScheduler().runTask(Quake.get(), () -> {
                boardByPlayer.forEach((p, b) -> b.render(p, placeholders));
                tryStartCountdown();
            });
        }
    }

    @EventHandler
    public void onArenaQuit(ArenaQuitEvent e) {
        if (arena.equals(e.getArena())) {
            clearPlayer(e.getPlayer());

            // ArenaQuitEvent is called before the player's actually quit (to permit cancellation).
            // For this reason, the code that has to read the new arena's players is run one tick later.
            Bukkit.getScheduler().runTask(
                    Quake.get(),
                    () -> boardByPlayer.forEach((p, b) -> b.render(p, placeholders))
            );
        }
    }

    @EventHandler
    public void onJoinSignUpdate(JoinSignUpdateEvent e) {
        if (arena.equals(e.getArena())) {
            e.getJoinSigns().forEach(sign -> joinSign.setSign(sign, null, placeholders));
        }
    }

    public static void loadConfig() {
        board = Quake.getConfigSection("lobby.waiting-board").get(SimpleBoardModel.class);
        joinSign = Quake.getConfigSection("join-signs").getMessage("waiting");
    }
}
