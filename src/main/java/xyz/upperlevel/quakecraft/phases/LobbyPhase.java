package xyz.upperlevel.quakecraft.phases;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
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
import xyz.upperlevel.uppercore.util.PlayerUtil;

import static xyz.upperlevel.quakecraft.Quake.get;

public class LobbyPhase implements Phase, Listener {
    private static Hotbar hotbar;

    @Getter
    private final QuakeArena arena;

    @Getter
    private final PhaseManager phaseManager = new PhaseManager();

    @Getter
    private final PlaceholderRegistry placeholderRegistry;

    public LobbyPhase(QuakeArena arena) {
        this.arena = arena;
        this.placeholderRegistry = arena.getPlaceholders();
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
    public void onEnable(Phase prev) {
        Bukkit.getPluginManager().registerEvents(this, get());
        arena.getPlayers().forEach(this::setupPlayer);
        phaseManager.setPhase(new WaitingPhase(this));
        Bukkit.getPluginManager().registerEvents(this, Quake.get());
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);
        phaseManager.setPhase(null);
        arena.getPlayers().forEach(this::clearPlayer);
        // player restore to original inventory/stats will be done by Uppercore
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onArenaJoin(ArenaJoinEvent e) {
        if (arena.equals(e.getArena())) {
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
        Config config = Quake.getConfigSection("lobby");
        hotbar = config.get("lobby-hotbar", Hotbar.class);
    }
}
