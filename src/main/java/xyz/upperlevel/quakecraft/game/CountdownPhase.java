package xyz.upperlevel.quakecraft.game;

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
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.uppercore.board.Board;
import xyz.upperlevel.uppercore.board.BoardManager;
import xyz.upperlevel.uppercore.board.SimpleConfigBoard;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.hotbar.Hotbar;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;
import xyz.upperlevel.uppercore.placeholder.message.Message;
import xyz.upperlevel.uppercore.placeholder.message.MessageManager;
import xyz.upperlevel.uppercore.sound.CompatibleSound;
import xyz.upperlevel.uppercore.util.PlayerUtil;

import java.io.File;
import java.util.List;
import java.util.Map;

import static java.lang.String.valueOf;
import static xyz.upperlevel.quakecraft.Quakecraft.get;
import static xyz.upperlevel.uppercore.Uppercore.boards;
import static xyz.upperlevel.uppercore.Uppercore.hotbars;

@Data
public class CountdownPhase implements QuakePhase, Listener {
    private static Hotbar sampleHotbar;
    private static Board board;

    private static List<PlaceholderValue<String>> signLines;

    public static Sound ORB_PICKUP = CompatibleSound.getRaw("ENTITY_EXPERIENCE_ORB_PICKUP");

    private static Map<String, Message> countdownMsg;

    private final QuakeArena arena;
    private final PlaceholderRegistry placeholderRegistry;

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
            updateSigns();
            if (timer > 0)
                timer--;
            else {
                cancel();
                game.getPhaseManager().setPhase(new GamePhase(game));
            }
        }
    };

    public CountdownPhase(QuakeArena arena) {
        this.arena = arena;
        placeholderRegistry = PlaceholderRegistry.create(arena.getPlaceholderRegistry())
                .set("countdown", timer);
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

    private boolean tryStopCountdown() {
        if (game.getPlayers().size() < game.getMinPlayers()) {
            task.cancel();
            clearTick();
            parent.setPhase(new WaitingPhase(parent));
            return true;
        }
        return false;
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
    public void updateSigns() {
        game.setSignLines(signLines, PlaceholderRegistry.create(game.getPlaceholders()).set("countdown", timer));
    }

    @Override
    public void onEnable(Phase previous) {
        Bukkit.getPluginManager().registerEvents(this, get());
        for (Player player : arena.getPlayers()) {
            BoardManager.open(player, board, placeholderRegistry);
        }
        timer = Quakecraft.get().getConfig().getInt("lobby.countdown"); // todo parse before game
        task.runTaskTimer(get(), 0, 20);
        updateSigns();
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);
        task.cancel();
        clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onArenaJoin(ArenaEvent.PlayerJoin e) {
        if (arena.equals(e.getArena())) {
            setup(e.getPlayer());
            update();
            updateSigns();
        }
    }

    @EventHandler
    public void onArenaQuit(ArenaEvent.PlayerQuit e) {
        if (arena.equals(e.getArena())) {
            clear(e.getPlayer());
            if (!tryStopCountdown()) {
                updateSigns();
                update();
            }
        }
    }

    public static void loadConfig() {
        MessageManager msg = get().getMessages().getSection("lobby");
        countdownMsg = msg.load("countdown");
        sampleHotbar = Hotbar.deserialize(Quakecraft.get(), Config.fromYaml(new File(
                getPhaseFolder(),
                "countdown_hotbar.yml"
        )));
        board = SimpleConfigBoard.create(new File(getPhaseFolder(), "countdown_board.yml"));
        signLines = get().getMessages().getConfig().getConfig("game").getMessageStrList("countdown-sign");
    }
}
