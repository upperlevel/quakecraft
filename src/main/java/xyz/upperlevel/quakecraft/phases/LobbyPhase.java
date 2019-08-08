package xyz.upperlevel.quakecraft.phases;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRespawnEvent;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.uppercore.Uppercore;
import xyz.upperlevel.uppercore.arena.Phase;
import xyz.upperlevel.uppercore.arena.PhaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.hotbar.Hotbar;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.util.PlayerUtil;

import static xyz.upperlevel.quakecraft.Quake.get;

public class LobbyPhase extends PhaseManager {
    private static Hotbar hotbar;

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

    @Override
    public boolean onJoin(Player player) {
        super.onJoin(player);
        setupPlayer(player);
        return false;
    }

    @Override
    public boolean onQuit(Player player) {
        super.onQuit(player);
        clearPlayer(player);
        return false;
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
