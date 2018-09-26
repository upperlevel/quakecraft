package xyz.upperlevel.quakecraft.game;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.events.GameJoinEvent;
import xyz.upperlevel.quakecraft.events.GameQuitEvent;
import xyz.upperlevel.uppercore.board.Board;
import xyz.upperlevel.uppercore.board.BoardManager;
import xyz.upperlevel.uppercore.board.SimpleConfigBoard;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.game.Phase;
import xyz.upperlevel.uppercore.hotbar.Hotbar;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.io.File;
import java.util.List;

import static xyz.upperlevel.uppercore.Uppercore.hotbars;

@Data
public class WaitingPhase implements QuakePhase, Listener {
    private static Hotbar sampleHotbar;
    private static Board board;

    private static List<PlaceholderValue<String>> signLines;

    private final Game game;
    private final LobbyPhase parent;

    private Hotbar hotbar;

    public WaitingPhase(LobbyPhase parent) {
        this.game = parent.getGame();
        this.parent = parent;
        this.hotbar = sampleHotbar;
    }

    private void setup(Player player) {
        //-------------------------hotbar
        if (hotbar != null) {
            hotbars().view(player).addHotbar(hotbar);
            player.updateInventory();
        }
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
        BoardManager.update(player, )

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
        sampleHotbar = Hotbar.deserialize(Quakecraft.get(), Config.fromYaml(new File(
                getPhaseFolder(),
                "waiting_hotbar.yml"
        )));
        board = SimpleConfigBoard.create(new File(getPhaseFolder(), "waiting_board.yml"));
        signLines = Quakecraft.get().getMessages().getConfig().getConfig("game").getMessageStrList("waiting-sign");
    }
}