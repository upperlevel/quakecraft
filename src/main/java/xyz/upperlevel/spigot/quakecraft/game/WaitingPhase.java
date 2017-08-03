package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.events.GameJoinEvent;
import xyz.upperlevel.spigot.quakecraft.events.GameQuitEvent;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.ConfigUtils;
import xyz.upperlevel.uppercore.game.Phase;
import xyz.upperlevel.uppercore.hotbar.Hotbar;

import static xyz.upperlevel.uppercore.Uppercore.boards;
import static xyz.upperlevel.uppercore.Uppercore.hotbars;

@Data
public class WaitingPhase implements Phase, Listener {
    private static Hotbar sampleHotbar;
    private static WaitingBoard sampleBoard;
    private final Game game;
    private final LobbyPhase parent;

    private Hotbar hotbar;
    private WaitingBoard board;

    public WaitingPhase(LobbyPhase parent) {
        this.game = parent.getGame();
        this.parent = parent;
        this.hotbar = sampleHotbar;
        this.board = new WaitingBoard(this, sampleBoard);
    }

    private void setup(Player player) {
        //-------------------------hotbar
        if (hotbar != null)
            hotbars().view(player).addHotbar(hotbar);
        //-------------------------board
        if (board != null)
            boards().view(player).setBoard(board);
    }

    private void clear(Player player) {
        //-------------------------hotbar
        hotbars().view(player).removeHotbar(hotbar);
        //-------------------------board
        boards().view(player).clear();
    }

    private void clear() {
        for (Player p : game.getPlayers())
            clear(p);
    }

    private void tryStart() {
        if (game.getPlayers().size() >= game.getMinPlayers())
            parent.setPhase(new CountdownPhase(parent));
    }

    private void update(Player player) {
        boards().view(player).render();
    }

    private void update() {
        for (Player player : game.getPlayers())
            update(player);
    }

    @Override
    public void onEnable(Phase previous) {
        Bukkit.getPluginManager().registerEvents(this, QuakeCraftReloaded.get());
        for (Player player : game.getPlayers())
            setup(player);
        tryStart();
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);
        clear();
    }

    @EventHandler
    public void onGameJoin(GameJoinEvent e) {
        if (e.getGame().equals(game)) {
            setup(e.getPlayer());
            update();
            tryStart();
        }
    }

    @EventHandler
    public void onGameQuit(GameQuitEvent e) {
        if (e.getGame().equals(game)) {
            clear(e.getPlayer());
        }
    }

    public static void loadConfig() {
        sampleHotbar = Hotbar.deserialize(QuakeCraftReloaded.get(), Config.wrap(ConfigUtils.loadConfig(
                QuakeCraftReloaded.get().getHotbars().getFolder(),
                "waiting-solo.yml"
        )));

        sampleBoard = WaitingBoard.deserialize(Config.wrap(ConfigUtils.loadConfig(
                QuakeCraftReloaded.get().getBoards().getFolder(),
                "waiting-solo.yml"
        )));
    }
}
