package xyz.upperlevel.spigot.quakecraft.game;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.core.Phase;

public class EndPhase implements Phase, Listener {

    private final Game game;
    private final GamePhase parent;

    public EndPhase(GamePhase parent) {
        this.game = parent.getGame();
        this.parent = parent;
    }

    private final BukkitRunnable endTask = new BukkitRunnable() {
        @Override
        public void run() {
            game.getPhaseManager().setPhase(new LobbyPhase(game));
        }
    };

    @Override
    public void onEnable(Phase previous) {
        GamePlayer winner = parent.getWinner();
        for (Player p : parent.getPlayers().keySet())
            p.sendMessage(winner.getName() + " win the match!");
        endTask.runTaskLater(QuakeCraftReloaded.get(), 20 * 10);
    }

    @Override
    public void onDisable(Phase next) {
        endTask.cancel();
    }
}
