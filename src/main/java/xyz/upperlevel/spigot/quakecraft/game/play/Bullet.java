package xyz.upperlevel.spigot.quakecraft.game.play;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.core.math.RayTrace;
import xyz.upperlevel.spigot.quakecraft.core.particle.Particle;
import xyz.upperlevel.spigot.quakecraft.events.LaserHitEvent;
import xyz.upperlevel.spigot.quakecraft.events.LaserSpreadEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static xyz.upperlevel.spigot.quakecraft.core.PlayerUtil.forEveryPlayerAround;

public class Bullet {
    public static final int MILLIS_IN_TICK = 50;

    public static final double LASER_BLOCKS_PER_TICK = 5;
    public static final double LASER_BLOCKS_INTERVAL = 0.25;
    public static final int EXP_UPDATE_EVERY = 2;

    private final static Set<Player> cooldowns = new HashSet<>();

    private final PlayingPhase phase;

    private final Player player;
    private final QuakePlayer qp;
    private final List<Vector> positions;
    private BukkitTask laserSpreader;

    private List<Particle> particles;

    private long cooldownMillis;
    private long shootTime = -1;
    private BukkitTask notifier;

    private int positionIndex;

    public Bullet(PlayingPhase phase, Player player) {
        this.phase = phase;
        this.player = player;
        this.qp = QuakeCraftReloaded.get().getPlayerManager().getPlayer(player);
        this.positions = new RayTrace(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection()).traverse(150, 0.25);
        positionIndex = 0;

        this.cooldownMillis = (long) (qp.getSelectedTrigger().getFiringSpeed() * 1000);
        this.particles = Collections.unmodifiableList(qp.getSelectedMuzzle().getParticles());
    }

    public void bang() {
        if(shootTime >= 0)
            throw new IllegalStateException("Already shot!");

        BukkitScheduler scheduler = Bukkit.getScheduler();
        laserSpreader = scheduler.runTaskTimer(QuakeCraftReloaded.get(), this::laserSpreaderRun, 0, 1);

        shootTime = System.currentTimeMillis();
        notifier = scheduler.runTaskTimer(QuakeCraftReloaded.get(), this::updatePlayer, EXP_UPDATE_EVERY, EXP_UPDATE_EVERY);
        player.setExp(1f);
        scheduler.runTaskLater(QuakeCraftReloaded.get(), this::cooldownEnd, cooldownMillis/MILLIS_IN_TICK);
    }

    public void laserSpreaderRun() {
        for (double distance = 0; distance < LASER_BLOCKS_PER_TICK; distance += LASER_BLOCKS_INTERVAL) {
            Vector pos = positions.get(positionIndex++);
            Location loc = pos.toLocation(player.getWorld());
            // laser spread
            {
                LaserSpreadEvent e = new LaserSpreadEvent(phase, loc, player, particles);
                Bukkit.getPluginManager().callEvent(e);
                if (e.isCancelled()) {
                    cancelSpreader();
                    break;
                }
            }
            // laser hit
            // todo choose radius
            // TODO: what if we search backwards? players -> chunk -> Bounding box?
            forEveryPlayerAround(player, loc, 0.25, hit -> {
                if (phase.getGame().isPlaying(hit)) {
                    LaserHitEvent e = new LaserHitEvent(phase, loc, player, hit);
                    Bukkit.getPluginManager().callEvent(e);
                    if (!e.isCancelled()) {
                        cancelSpreader();
                    }
                }
            });
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
        float perc = 1f - ((System.currentTimeMillis() - shootTime) / (float) cooldownMillis);
        player.setExp(perc < 0f ? 0f : perc);
    }

    public void cooldownEnd() {
        notifier.cancel();
        cooldowns.remove(player);
        player.setExp(0f);
    }

    public static boolean shoot(PlayingPhase phase, Player player) {
        if (!cooldowns.add(player))
            return false;
        new Bullet(phase, player).bang();
        return true;
    }
}