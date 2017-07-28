package xyz.upperlevel.spigot.quakecraft.game.play;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.core.math.RayTrace;
import xyz.upperlevel.spigot.quakecraft.events.LaserHitEvent;
import xyz.upperlevel.spigot.quakecraft.events.LaserSpreadEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Bullet {
    public static final int MILLIS_IN_TICK = 50;

    public static final double LASER_BLOCKS_PER_TICK = 5;
    public static final double LASER_BLOCKS_INTERVAL = 0.25;
    public static final int EXP_UPDATE_EVERY = 4;

    private final static Set<Player> cooldowns = new HashSet<>();

    private final PlayingPhase phase;

    private final Player player;
    private final List<Vector> positions;
    private BukkitTask laserSpreader;

    private long cooldownMillis = 2000;//TODO: config
    private long shootTime = -1;
    private BukkitTask notifier;

    private int positionIndex;

    public Bullet(PlayingPhase phase, Player player) {
        this.phase = phase;
        this.player = player;
        this.positions = new RayTrace(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection()).traverse(150, 0.25);
        positionIndex = 0;
    }

    public void bang() {
        if(shootTime < 0)
            throw new IllegalStateException("Already shot!");

        BukkitScheduler scheduler = Bukkit.getScheduler();
        laserSpreader = scheduler.runTaskTimer(QuakeCraftReloaded.get(), this::laserSpreaderRun, 0, 1);

        shootTime = System.currentTimeMillis();
        notifier = scheduler.runTaskTimer(QuakeCraftReloaded.get(), this::updatePlayer, EXP_UPDATE_EVERY, EXP_UPDATE_EVERY);
        scheduler.runTaskLater(QuakeCraftReloaded.get(), this::cooldownEnd, cooldownMillis/MILLIS_IN_TICK);
    }

    public void laserSpreaderRun() {
        for (double distance = 0; distance < LASER_BLOCKS_PER_TICK; distance += LASER_BLOCKS_INTERVAL) {
            Vector pos = positions.get(positionIndex++);
            Location loc = pos.toLocation(player.getWorld());
            // laser spread
            {
                LaserSpreadEvent e = new LaserSpreadEvent(phase, loc, player);
                Bukkit.getPluginManager().callEvent(e);
                if (e.isCancelled()) {
                    cancelSpreader();
                    break;
                }
            }
            // laser hit
            Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, 0.25, 0.25, 0.25); // todo choose radius
            for (Entity entity : entities) {
                if (entity instanceof Player && !entity.equals(player)) {
                    Player hit = (Player) entity;
                    if (phase.getGame().isPlaying(hit)) {
                        LaserHitEvent e = new LaserHitEvent(phase, loc, player, hit);
                        Bukkit.getPluginManager().callEvent(e);
                        if (!e.isCancelled()) {
                            cancelSpreader();
                            break;
                        }
                    }
                }
            }
            // laser hit block
            if (loc.getBlock().getType().isSolid()) {
                cancelSpreader();
                break;
            }
            if (positionIndex == positions.size()) {
                cancelSpreader();
                break;
            }
        }
    }

    public void cancelSpreader() {
        laserSpreader.cancel();
    }

    public void updatePlayer() {
        float perc = (System.currentTimeMillis() - shootTime) / (float) cooldownMillis;
        player.setExp(perc > 1f ? 1f : perc);
    }

    public void cooldownEnd() {
        notifier.cancel();
        cooldowns.remove(player);
    }

    public static boolean shoot(PlayingPhase phase, Player player) {
        if (!cooldowns.add(player))
            return false;
        new Bullet(phase, player).bang();
        return true;
    }
}