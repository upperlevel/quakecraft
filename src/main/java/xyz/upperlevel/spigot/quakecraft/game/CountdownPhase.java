package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.spigot.quakecraft.core.PlayerUtil;
import xyz.upperlevel.spigot.quakecraft.events.GameJoinEvent;
import xyz.upperlevel.spigot.quakecraft.events.GameQuitEvent;
import xyz.upperlevel.uppercore.Uppercore;
import xyz.upperlevel.uppercore.board.Board;
import xyz.upperlevel.uppercore.board.BoardManager;
import xyz.upperlevel.uppercore.board.BoardView;
import xyz.upperlevel.uppercore.hotbar.Hotbar;
import xyz.upperlevel.uppercore.hotbar.HotbarManager;

import java.io.File;

import static org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;
import static xyz.upperlevel.uppercore.Uppercore.boards;
import static xyz.upperlevel.uppercore.Uppercore.hotbars;

@Data
public class CountdownPhase implements Phase, Listener {

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
                player.playSound(player.getLocation(), ENTITY_EXPERIENCE_ORB_PICKUP, 0, 100f);
                boards().view(player).render();
            }
            if (timer > 0)
                timer--;
            else {
                cancel();
                parent.setPhase(new GamePhase(game));
            }
        }
    };

    public CountdownPhase(LobbyPhase parent) {
        this.game = parent.getGame();
        this.parent = parent;
        // HOTBAR
        {
            File file = new File(get().getHotbars().getFolder(), "countdown_solo.yml");
            if (file.exists())
                hotbar = Hotbar.deserialize(get(), YamlConfiguration.loadConfiguration(file)::get);
            else {
                QuakeCraftReloaded.get().getLogger().severe("Could not find file: \"" + file + "\"");
            }
        }
        // BOARD
        {
            File file = new File(get().getBoards().getFolder(), "countdown_solo.yml");
            if (file.exists())
                board = CountdownBoard.deserialize(this, YamlConfiguration.loadConfiguration(file)::get);
            else {
                QuakeCraftReloaded.get().getLogger().severe("Could not find file: \"" + file + "\"");
            }
        }
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
            clearTick(p);
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

    @Override
    public void onEnable(Phase previous) {
        Bukkit.getPluginManager().registerEvents(this, get());
        for (Player player : game.getPlayers())
            setup(player);
        timer = 10;
        task.runTaskTimer(get(), 0, 20);
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);
        task.cancel();
        clear();
    }

    @EventHandler
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
}
