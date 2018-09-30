package xyz.upperlevel.quakecraft.phases;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.quakecraft.events.LaserHitEvent;
import xyz.upperlevel.quakecraft.game.playing.MultiStab;
import xyz.upperlevel.uppercore.math.RayTrace;
import xyz.upperlevel.uppercore.particle.Particle;
import xyz.upperlevel.uppercore.sound.PlaySound;
import xyz.upperlevel.uppercore.util.FireworkUtil;
import xyz.upperlevel.uppercore.util.PlayerUtil;

import java.util.*;

public class Laser {
    public static final int MILLIS_IN_TICK = 50;

    public static final double LASER_BLOCKS_PER_TICK = 5;
    public static final double LASER_BLOCKS_INTERVAL = 0.25;
    public static final int EXP_UPDATE_EVERY = 2;

    private final static Set<Player> cooldowns = new HashSet<>();

    public static double headshotHeight = 1.35;
    public static PlaySound shootSound;

    @Getter
    private final PlayingPhase playingPhase;

    @Getter
    private final GamePhase gamePhase;

    @Getter
    private final QuakeArena arena;

    @Getter
    private final Player player;

    @Getter
    private final QuakeAccount account;

    @Getter
    private final Gamer shooter;

    private final List<Vector> positions;
    private int positionIndex;
    private BukkitTask laserSpreader;

    private List<Particle> particles;

    private int cooldownTicks;
    private long shootTime = -1;
    private BukkitTask notifier;

    @Getter
    private Set<Gamer> alreadyHit = new HashSet<>(); // this is done to avoid hitting the same player twice

    public Laser(PlayingPhase playingPhase, Player player) {
        this.playingPhase = playingPhase;
        this.gamePhase = playingPhase.getGamePhase();
        this.arena = gamePhase.getArena();

        this.player = player;
        this.account = Quake.getAccount(player);
        this.shooter = gamePhase.getGamer(player);

        Vector start = player.getEyeLocation().toVector();
        start.setY(start.getY() - 0.15);
        Vector direction = player.getEyeLocation().getDirection();
        this.positions = new RayTrace(start, direction).traverse(150, 0.25);
        positionIndex = 0;

        this.cooldownTicks = (int) Math.ceil(account.getSelectedTrigger().getFiringSpeed() * shooter.getGunCooldownBase());
        this.particles = Collections.unmodifiableList(account.getSelectedMuzzle().getParticles());
    }

    public void bang() {
        if (shootTime >= 0)
            throw new IllegalStateException("Already shot!");

        shootSound.play(player);

        // start spreading the shoot
        new Spreader().runTaskTimer(Quake.get(), 0, 1);

        BukkitScheduler scheduler = Bukkit.getScheduler();
        shootTime = System.currentTimeMillis();
        notifier = scheduler.runTaskTimer(Quake.get(), this::updatePlayer, EXP_UPDATE_EVERY, EXP_UPDATE_EVERY);
        player.setExp(1f);
        scheduler.runTaskLater(Quake.get(), this::cooldownEnd, cooldownTicks);
    }

    public void halt() {
        laserSpreader.cancel();
        MultiStab.tryReach(gamePhase, this);
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
        if (!cooldowns.add(player)) {
            return false;
        }
        new Laser(phase, player).bang();
        return true;
    }

    public boolean isHeadshot(Player player, Location loc) {
        return loc.getY() - player.getLocation().getY() > headshotHeight;
    }

    private void explodeBarrel(Location location, QuakeAccount shooter) {
        FireworkEffect.Type type = shooter.getSelectedBarrel().getFireworkType();
        Color color = shooter.getSelectedLaser().getFireworkColor();
        FireworkUtil.instantFirework(
                location,
                FireworkEffect.builder()
                        .with(type)
                        .withColor(color)
                        .build());
    }

    public class Spreader extends BukkitRunnable {
        /**
         * Gets a list of players that get hits by the
         * current step of laser spread.
         */
        private List<Gamer> getHitGamers(Location step) {
            List<Gamer> r = new ArrayList<>();
            PlayerUtil.forEveryPlayerAround(player, step, 0.25, hit -> {
                Gamer g = gamePhase.getGamer(hit);
                if (g != null && !alreadyHit.contains(g)) {
                    r.add(g);
                }
            });
            return r;
        }

        @Override
        public void run() {
            for (double distance = 0; distance < LASER_BLOCKS_PER_TICK; distance += LASER_BLOCKS_INTERVAL) {
                Location loc = positions.get(positionIndex++).toLocation(player.getWorld());

                // displays the particles each step it does
                particles.forEach(part -> part.display(loc, gamePhase.getArena().getPlayers()));

                // foreach player shoot
                getHitGamers(loc).forEach(hit -> {
                    LaserHitEvent e = new LaserHitEvent(
                            arena,
                            shooter,
                            hit,
                            isHeadshot(hit.getPlayer(), loc)
                    );
                    Bukkit.getPluginManager().callEvent(e);

                    if (e.isCancelled()) {
                        return;
                    }

                    account.getSelectedKillSound().play(loc);
                    explodeBarrel(loc, account);

                    alreadyHit.add(hit);
                });

                // if laser hits block is cancelled
                if (loc.getBlock().getType().isSolid()) {
                    Laser.this.halt();
                }

                // if positions ends laser is cancelled
                if (positionIndex == positions.size()) {
                    Laser.this.halt();
                }
            }
        }
    }
}