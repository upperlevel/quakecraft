package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.spigot.quakecraft.event.GameJoinEvent;
import xyz.upperlevel.spigot.quakecraft.event.GameQuitEvent;
import xyz.upperlevel.uppercore.gui.hotbar.Hotbar;
import xyz.upperlevel.uppercore.gui.hotbar.HotbarSystem;
import xyz.upperlevel.uppercore.scoreboard.Board;
import xyz.upperlevel.uppercore.scoreboard.ScoreboardSystem;

import static org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;

@Data
public class CountdownPhase implements Phase, Listener {

    private final Game game;
    private final LobbyPhase parent;
    private final Hotbar hotbar;

    private int timer;
    private final BukkitRunnable task = new BukkitRunnable() {
        @Override
        public void run() {
            if (timer > 0)
                timer--;
            else {
                cancel();
                parent.setPhase(new MatchPhase(game));
            }
            for (Player player : game.getPlayers()) {
                player.setLevel(timer);
                player.playSound(player.getLocation(), ENTITY_EXPERIENCE_ORB_PICKUP, 0, 100f);
                ScoreboardSystem.view(player).update();
            }
        }
    };

    private void setup(Player player) {
        HotbarSystem.view(player).addHotbar(hotbar);
        Board board = get().getScoreboards().get("solo_quake_countdown");
        if (board != null)
            ScoreboardSystem.view(player).setBoard(board);
    }

    private void update(Player player) {
        ScoreboardSystem.view(player).update();
    }

    private void update() {
        for (Player player : game.getPlayers())
            update(player);
    }

    public CountdownPhase(LobbyPhase parent) {
        this.game = parent.getGame();
        this.parent = parent;
        hotbar = get().getHotbars().get("solo_quake_countdown_hotbar");
    }

    @Override
    public void onEnable(Phase previous) {
        Bukkit.getPluginManager().registerEvents(this, QuakeCraftReloaded.get());
        for (Player player : game.getPlayers())
            setup(player);
        timer = 10;
        task.runTaskTimer(QuakeCraftReloaded.get(), 0, 20);
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);
        task.cancel();
        for (Player player : game.getPlayers())
            HotbarSystem.view(player).removeHotbar(hotbar);
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
            ScoreboardSystem.view(e.getPlayer()).clear();
            update();
        }
    }
}
