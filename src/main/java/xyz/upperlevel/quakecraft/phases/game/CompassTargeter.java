package xyz.upperlevel.quakecraft.phases.game;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.phases.game.GamePhase;
import xyz.upperlevel.uppercore.arena.Arena;
import xyz.upperlevel.uppercore.util.Dbg;

public class CompassTargeter extends BukkitRunnable {
    public static final long delay = 20;

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
                double dist = loc1.distance(player.getLocation());
                if (dist < minDist) {
                    nearest = player;
                    minDist = dist;
                }
            }
        }
        return nearest;
    }

    @Override
    public void run() {
        gamePhase.getGamers()
                .stream()
                .map(Gamer::getPlayer)
                .forEach(subject -> {
                    Player target = getNearestGamer(subject);
                    if (target != null) { // Could be null, but actually should never.
                        subject.setCompassTarget(target.getLocation());
                    }
                    //Dbg.pf("Updating compass target - player: %s - target: %s", subject.getName(), target != null ? target.getName() : "null");
                });
    }
}
