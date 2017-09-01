package xyz.upperlevel.quakecraft.game.countdown;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.events.GameJoinEvent;
import xyz.upperlevel.quakecraft.events.GameQuitEvent;
import xyz.upperlevel.quakecraft.game.Game;
import xyz.upperlevel.quakecraft.game.GamePhase;
import xyz.upperlevel.quakecraft.game.LobbyPhase;
import xyz.upperlevel.quakecraft.game.waiting.WaitingPhase;
import xyz.upperlevel.uppercore.board.BoardView;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.ConfigUtils;
import xyz.upperlevel.uppercore.game.Phase;
import xyz.upperlevel.uppercore.hotbar.Hotbar;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;
import xyz.upperlevel.uppercore.sound.CompatibleSound;
import xyz.upperlevel.uppercore.util.PlayerUtil;

import java.io.File;
import java.util.Map;

import static java.lang.String.valueOf;
import static xyz.upperlevel.quakecraft.Quakecraft.get;
import static xyz.upperlevel.uppercore.Uppercore.boards;
import static xyz.upperlevel.uppercore.Uppercore.hotbars;

@Data
public class CountdownPhase implements Phase, Listener {
    private static Hotbar sampleHotbar;
    private static CountdownBoard sampleBoard;
    public static Sound ORB_PICKUP = CompatibleSound.getRaw("ENTITY_EXPERIENCE_ORB_PICKUP");

    private static Map<String, Message> countdownMsg;

    private final Game game;
    private final LobbyPhase parent;

    private Hotbar hotbar;
    private CountdownBoard board;

    private int timer;
    private final BukkitRunnable task = new BukkitRunnable() {
        @Override
        public void run() {
            for (Player player : game.getPlayers()) {
                player.setLevel(timer);
                player.playSound(player.getLocation(), ORB_PICKUP, 0, 100f);
                boards().view(player).render();

                // msg.get(timer) -> not so nice
                Message msg = countdownMsg.get(valueOf(timer));
                if (msg != null)
                    msg.send(player);
            }
            if (timer > 0)
                timer--;
            else {
                cancel();
                game.getPhaseManager().setPhase(new GamePhase(game));
            }
        }
    };

    public CountdownPhase(LobbyPhase parent) {
        this.game = parent.getGame();
        this.parent = parent;
        this.hotbar = sampleHotbar;
        this.board = new CountdownBoard(this, sampleBoard);
    }

    private void setup(Player player) {
        PlayerUtil.clearInventory(player);
        PlayerUtil.restore(player);
        // HOTBAR
        if (hotbar != null)
            hotbars().view(player).addHotbar(hotbar);

        // BOARD
        if (board != null)
            boards().view(player).setBoard(board);
    }

    private void clearTick(Player player) {
        player.setLevel(0);
    }

    private void clearTick() {
        for (Player p : game.getPlayers())
            clearTick(p);
    }

    private void clear(Player player) {
        hotbars().view(player).removeHotbar(hotbar);
        BoardView view = boards().view(player);
        if (view != null)
            view.clear();
        clearTick(player);
    }

    private void clear() {
        for (Player p : game.getPlayers()) {
            clear(p);
        }
    }

    private void tryStop() {
        if (game.getPlayers().size() < game.getMinPlayers()) {
            task.cancel();
            clearTick();
            parent.setPhase(new WaitingPhase(parent));
        }
    }

    private void update(Player player) {
        boards().view(player).render();
    }

    private void update() {
        for (Player player : game.getPlayers())
            update(player);
    }

    private static File getPhaseFolder() {
        return new File(Quakecraft.get().getDataFolder(), "game/countdown");
    }

    @Override
    public void onEnable(Phase previous) {
        Bukkit.getPluginManager().registerEvents(this, get());
        for (Player player : game.getPlayers())
            setup(player);
        timer = Quakecraft.get().getConfig().getInt("lobby.countdown"); // todo parse before game
        task.runTaskTimer(get(), 0, 20);
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);
        task.cancel();
        clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onGameJoin(GameJoinEvent e) {
        if (e.getGame().equals(game)) {
            setup(e.getPlayer());
            update();
        }
    }

    @EventHandler
    public void onGameQuit(GameQuitEvent e) {
        if (e.getGame().equals(game)) {
            tryStop();
            clear(e.getPlayer());
            update();
        }
    }

    public static void loadConfig() {
        MessageManager msg = get().getMessages().getSection("lobby");
        countdownMsg = msg.load("countdown");
        sampleHotbar = Hotbar.deserialize(Quakecraft.get(), Config.wrap(ConfigUtils.loadConfig(
                getPhaseFolder(),
                "countdown_hotbar.yml"
        )));

        sampleBoard = CountdownBoard.deserialize(Config.wrap(ConfigUtils.loadConfig(
                getPhaseFolder(),
                "countdown_board.yml"
        )));
    }
}
