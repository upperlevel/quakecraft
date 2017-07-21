package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.spigot.quakecraft.events.GameJoinEvent;
import xyz.upperlevel.spigot.quakecraft.events.GameQuitEvent;
import xyz.upperlevel.uppercore.gui.hotbar.Hotbar;
import xyz.upperlevel.uppercore.gui.hotbar.HotbarSystem;
import xyz.upperlevel.uppercore.scoreboard.Board;
import xyz.upperlevel.uppercore.scoreboard.BoardView;
import xyz.upperlevel.uppercore.scoreboard.ScoreboardSystem;

import static org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;

@Data
public class CountdownPhase implements Phase, Listener {

    private final Game game;
    private final LobbyPhase parent;

    private final Hotbar hotbar;
    private final Board board;

    private int timer;
    private final BukkitRunnable task = new BukkitRunnable() {
        @Override
        public void run() {
            for (Player player : game.getPlayers()) {
                player.setLevel(timer);
                player.playSound(player.getLocation(), ENTITY_EXPERIENCE_ORB_PICKUP, 0, 100f);
                ScoreboardSystem.view(player).update();
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

        hotbar = get().getHotbars().get("countdown_solo");
        board = get().getScoreboards().get("countdown_solo");
    }

    private void setup(Player player) {
        //-------------------------hotbar
        if (hotbar != null)
            HotbarSystem.view(player).addHotbar(hotbar);
        else
            get().getLogger().info("Hotbar not found: \"countdown_solo\"");
        //-------------------------scoreboard
        if (board != null)
            ScoreboardSystem.view(player).setBoard(board);
        else
            get().getLogger().info("Scoreboard not found: \"countdown_solo\"");
    }

    private void clearTick(Player player) {
        player.setLevel(0);
    }

    private void clearTick() {
        for (Player p : game.getPlayers())
            clearTick(p);
    }

    private void clear(Player player) {
        //-------------------------hotbar
        HotbarSystem.view(player).removeHotbar(hotbar);
        //-------------------------scoreboard
        BoardView view = ScoreboardSystem.view(player);
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
        ScoreboardSystem.view(player).update();
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
