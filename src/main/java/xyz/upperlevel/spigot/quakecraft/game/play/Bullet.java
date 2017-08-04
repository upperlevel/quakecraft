package xyz.upperlevel.spigot.quakecraft.game.play;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.uppercore.math.RayTrace;
import xyz.upperlevel.uppercore.particle.Particle;
import xyz.upperlevel.spigot.quakecraft.events.LaserHitEvent;
import xyz.upperlevel.spigot.quakecraft.events.LaserStabEvent;
import xyz.upperlevel.spigot.quakecraft.events.LaserSpreadEvent;
import xyz.upperlevel.spigot.quakecraft.game.Participant;

import java.util.*;

import static xyz.upperlevel.uppercore.util.PlayerUtil.forEveryPlayerAround;

public class Bullet {
    public static double headshotHeight = 1.35;
    public static final int MILLIS_IN_TICK = 50;

    public static final double LASER_BLOCKS_PER_TICK = 5;
    public static final double LASER_BLOCKS_INTERVAL = 0.25;
    public static final int EXP_UPDATE_EVERY = 2;

    private final static Set<Player> cooldowns = new HashSet<>();

    @Getter
    private final PlayingPhase phase;

    @Getter
    private final Player player;
    @Getter
    private final QuakePlayer qp;
    @Getter
    private final Participant participant;
    private final List<Vector> positions;
    private int positionIndex;
    private BukkitTask laserSpreader;

    private List<Particle> particles;

    private int cooldownTicks;
    private long shootTime = -1;
    private BukkitTask notifier;

    @Getter
    private List<Player> killed = new ArrayList<>();

    public Bullet(PlayingPhase phase, Player player) {
        this.phase = phase;
        this.player = player;
        this.qp = QuakeCraftReloaded.get().getPlayerManager().getPlayer(player);
        this.participant = phase.getParent().getParticipant(player);
        Vector start = player.getEyeLocation().toVector();
        start.setY(start.getY() - 0.15);
        Vector direction = player.getEyeLocation().getDirection();
        this.positions = new RayTrace(start, direction).traverse(150, 0.25);
        positionIndex = 0;

        this.cooldownTicks = (int) Math.ceil(qp.getSelectedTrigger().getFiringSpeed() * participant.getGunCooldownBase());
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
        scheduler.runTaskLater(QuakeCraftReloaded.get(), this::cooldownEnd, cooldownTicks);
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
                    stopLaser();
                    break;
                }
            }
            // laser hit
            // todo choose radius
            // TODO: what if we search backwards? players -> chunk -> Bounding box?

            forEveryPlayerAround(player, loc, 0.25, hit -> {
                if (phase.getGame().isPlaying(hit)) {
                    LaserStabEvent e = new LaserStabEvent(phase, loc, qp, player, hit, isHeadshot(hit, loc));
                    Bukkit.getPluginManager().callEvent(e);
                    if (!e.isCancelled()) {
                        killed.add(player);
                    }
                }
            });
            // laser hit block
            if (loc.getBlock().getType().isSolid()) {
                stopLaser();
                break;
            }
            if (positionIndex == positions.size()) {
                stopLaser();
                break;
            }
        }
    }

    public void stopLaser() {
        laserSpreader.cancel();
        Bukkit.getPluginManager().callEvent(new LaserHitEvent(phase, qp, killed));
        MultiStab.tryReach(phase.getParent(), this);
    }

    public void updatePlayer() {
        float perc = 1f - ((System.currentTimeMillis() - shootTime) / (float) (cooldownTicks * MILLIS_IN_TICK));
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

    public boolean isHeadshot(Player player, Location loc) {
        return loc.getY() - player.getLocation().getY() > headshotHeight;
    }

    public static void loadConfig() {
        headshotHeight = QuakeCraftReloaded.get().getCustomConfig().getDoubleRequired("game.headshot-height");
    }
}