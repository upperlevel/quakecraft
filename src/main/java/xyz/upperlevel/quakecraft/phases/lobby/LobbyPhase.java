package xyz.upperlevel.quakecraft.phases.lobby;

import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.uppercore.Uppercore;
import xyz.upperlevel.uppercore.arena.Phase;
import xyz.upperlevel.uppercore.arena.PhaseManager;
import xyz.upperlevel.uppercore.arena.event.ArenaJoinEvent;
import xyz.upperlevel.uppercore.arena.event.ArenaQuitEvent;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.hotbar.Hotbar;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.util.Dbg;
import xyz.upperlevel.uppercore.util.PlayerUtil;

public class LobbyPhase extends PhaseManager {
    private static Hotbar hotbar;

    @Getter
    private final QuakeArena arena;

    @Getter
    private final PlaceholderRegistry<?> placeholders;

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
    }

    @EventHandler
    public void onArenaJoin(ArenaJoinEvent e) {
        if (arena.equals(e.getArena())) {
            if (arena.getPlayers().size() >= arena.getMaxPlayers()) {
                e.setCancelled(true);
                Dbg.pf("[%s] %s tried to join but the game is full!", arena.getName(), e.getPlayer().getName());
                return;
            }

            Player player = e.getPlayer();
            player.teleport(arena.getLobby());
            setupPlayer(player);
        }
    }

    @EventHandler
    public void onArenaQuit(ArenaQuitEvent e) {
        if (arena.equals(e.getArena())) {
            clearPlayer(e.getPlayer());
        }
    }

    // Actually the player should never die within a Quake arena.
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (arena.hasPlayer(e.getPlayer())) {
            e.setRespawnLocation(arena.getLobby());
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) return;

        Player player = (Player) e.getEntity();
        if (!arena.hasPlayer(player)) return;

        if (e.getCause() == EntityDamageEvent.DamageCause.VOID)
            player.teleport(arena.getLobby());
    }

    public static void loadConfig() {
        Config cfg = Quake.getConfigSection("lobby");
        hotbar = cfg.getRequired("lobby-hotbar", Hotbar.class);
    }
}
