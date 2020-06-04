package xyz.upperlevel.quakecraft.phases.game;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.phases.game.GamePhase;
import xyz.upperlevel.uppercore.arena.Arena;

public class CompassTargeter extends BukkitRunnable {
    public static final long delay = 20 * 2;

    private final Arena arena;
    private final GamePhase gamePhase;

    public CompassTargeter(GamePhase gamePhase) {
        this.arena = gamePhase.getArena();
        this.gamePhase = gamePhase;
    }

    public void start() {
        runTaskTimer(Quake.get(), 0, delay);
    }

    private Player getNearestGamer(Player subject) {
        Player nearest = null;
        double minDist = Double.POSITIVE_INFINITY;
        Location loc1 = subject.getLocation();
        for (Player player : arena.getPlayers()) {
            if (gamePhase.isGamer(player) && subject != player) {
                Location loc2 = player.getLocation();
                double dist = loc1.distance(loc2);
                if (minDist < dist) {
                    nearest = player;
                    minDist = dist;
                }
            }
        }
        return nearest;
    }

    @Override
    public void run() {
        for (Player subject : arena.getPlayers()) {
            Player target = getNearestGamer(subject);
            if (target != null) { // Could be null, but actually should never.
                subject.setCompassTarget(target.getLocation());
            }
        }
    }
}
