package xyz.upperlevel.quakecraft.phases.lobby;

import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRespawnEvent;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.uppercore.Uppercore;
import xyz.upperlevel.uppercore.arena.Phase;
import xyz.upperlevel.uppercore.arena.PhaseManager;
import xyz.upperlevel.uppercore.arena.events.ArenaJoinEvent;
import xyz.upperlevel.uppercore.arena.events.ArenaQuitEvent;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.hotbar.Hotbar;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.message.Message;
import xyz.upperlevel.uppercore.util.Dbg;
import xyz.upperlevel.uppercore.util.PlayerUtil;

public class LobbyPhase extends PhaseManager {
    private static Hotbar hotbar;

    private static Message maxPlayersReachedError;

    @Getter
    private final QuakeArena arena;

    @Getter
    private final PlaceholderRegistry placeholders;

    public LobbyPhase(QuakeArena arena) {
        super("lobby");

        this.arena = arena;
        this.placeholders = arena.getPlaceholders();
    }

    private void setupPlayer(Player player) {
        PlayerUtil.clearInventory(player);
        PlayerUtil.restore(player);
        player.setGameMode(GameMode.ADVENTURE);

        Uppercore.hotbars().view(player).addHotbar(hotbar);
    }

    private void clearPlayer(Player player) {
        Uppercore.hotbars().view(player).removeHotbar(hotbar);
    }

    @Override
    public void onEnable(Phase previousPhase) {
        super.onEnable(previousPhase);
        arena.getPlayers().forEach(this::onJoin);
        setPhase(new WaitingPhase(this));
    }

    @Override
    public void onDisable(Phase nextPhase) {
        super.onDisable(nextPhase);
        setPhase(null);
        arena.getPlayers().forEach(this::clearPlayer);
        // TODO player restore to original inventory/stats will be done by Uppercore
    }

    @EventHandler
    public void onArenaJoin(ArenaJoinEvent e) {
        if (arena.equals(e.getArena())) {
            if (arena.getPlayers().size() >= arena.getMaxPlayers()) {
                e.setCancelled(true);
                Dbg.pf("[%s] %s tried to join but the game is full!", arena.getName(), e.getPlayer().getName());
                return;
            }
            setupPlayer(e.getPlayer());
        }
    }

    @EventHandler
    public void onArenaQuit(ArenaQuitEvent e) {
        if (arena.equals(e.getArena())) {
            clearPlayer(e.getPlayer());
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (arena.hasPlayer(e.getPlayer())) {
            e.setRespawnLocation(arena.getLobby());
        }
    }

    public static void loadConfig() {
        Config cfg = Quake.getConfigSection("lobby");
        maxPlayersReachedError = Quake.get().getCustomConfig().getMessageRequired("messages.lobby.max-players-reached");
        hotbar = cfg.getRequired("lobby-hotbar", Hotbar.class);
    }
}
