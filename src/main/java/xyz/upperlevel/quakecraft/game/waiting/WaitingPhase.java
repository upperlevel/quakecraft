package xyz.upperlevel.quakecraft.game.waiting;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.events.GameJoinEvent;
import xyz.upperlevel.quakecraft.events.GameQuitEvent;
import xyz.upperlevel.quakecraft.game.Game;
import xyz.upperlevel.quakecraft.game.LobbyPhase;
import xyz.upperlevel.quakecraft.game.QuakePhase;
import xyz.upperlevel.quakecraft.game.countdown.CountdownPhase;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.ConfigUtils;
import xyz.upperlevel.uppercore.game.Phase;
import xyz.upperlevel.uppercore.hotbar.Hotbar;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.io.File;
import java.util.List;

import static xyz.upperlevel.uppercore.Uppercore.boards;
import static xyz.upperlevel.uppercore.Uppercore.hotbars;

@Data
public class WaitingPhase implements QuakePhase, Listener {
    private static Hotbar sampleHotbar;
    private static WaitingBoard sampleBoard;

    private static List<PlaceholderValue<String>> signLines;

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

    private void tryStartCountdown() {
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

    private static File getPhaseFolder() {
        return new File(Quakecraft.get().getDataFolder(), "game/waiting");
    }

    @Override
    public void updateSigns() {
        game.setSignLines(signLines, game.getPlaceholders());
    }

    @Override
    public void onEnable(Phase previous) {
        Bukkit.getPluginManager().registerEvents(this, Quakecraft.get());
        for (Player player : game.getPlayers())
            setup(player);
        tryStartCountdown();
        updateSigns();
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);
        clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onGameJoin(GameJoinEvent e) {
        if (e.getGame().equals(game)) {
            setup(e.getPlayer());
            update();
            tryStartCountdown();
            updateSigns();
        }
    }

    @EventHandler
    public void onGameQuit(GameQuitEvent e) {
        if (e.getGame().equals(game)) {
            clear(e.getPlayer());
            updateSigns();
        }
    }

    public static void loadConfig() {
        sampleHotbar = Hotbar.deserialize(Quakecraft.get(), Config.wrap(ConfigUtils.loadConfig(
                getPhaseFolder(),
                "waiting_hotbar.yml"
        )));
        sampleBoard = WaitingBoard.deserialize(Config.wrap(ConfigUtils.loadConfig(
                getPhaseFolder(),
                "waiting_board.yml"
        )));

        signLines = Quakecraft.get().getMessages().getConfig().getConfig("game").getMessageStrList("waiting-sign");
    }
}
