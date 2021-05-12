package xyz.upperlevel.quakecraft.phases.game;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.quakecraft.shop.railgun.Railgun;
import xyz.upperlevel.uppercore.arena.Arena;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;
import xyz.upperlevel.uppercore.placeholder.message.Message;
import xyz.upperlevel.uppercore.util.Dbg;
import xyz.upperlevel.uppercore.util.FireworkUtil;

import java.util.List;
import java.util.stream.Collectors;

public class Shot extends BukkitRunnable {
    /**
     * Imagine the ray as a cylinder. The size is half of the diameter of the cylinder's base.
     */
    public static double raySize;// = 0.4;

    /**
     * The distance from the origin after when the projectile can be destroyed.
     */
    public static double maxDistance;// = 150.0;

    /**
     * The speed of the projectile expressed in m/s (or blocks per second).
     */
    public static double speed;// = 100.0;

    /**
     * The step that must be used for computing the players intersection and spawning the particles.
     * It's measured in blocks.
     */
    public static double rayStep;// = 0.5;

    public static PlaceholderValue<String> defaultKillMessage;
    public static Message shotMessage;
    public static Message headshotMessage;
    public static Message cantHitPlayerBecauseRespawningMsg;

    private final Arena arena;
    private final GamePhase gamePhase;

    private final Player shooter;
    private final Profile shooterAccount;

    private final Location location;
    private final Vector direction;
    private double distance;

    private boolean started = false;

    public Shot(GamePhase gamePhase, Player shooter) {
        this.arena = gamePhase.getArena();
        this.gamePhase = gamePhase;

        this.shooter = shooter;
        this.shooterAccount = Quake.getProfileController().getOrCreateProfile(shooter);

        this.location = shooter.getEyeLocation();
        this.direction = this.location.getDirection();
    }

    public void start() {
        if (started)
            throw new IllegalStateException("This shot has already been started, can't start it anymore.");
        this.started = true;
        super.runTaskTimer(Quake.get(), 0, 1);
    }

    private void kill(List<Player> hits) {

        int hitsCount = 0;

        for (Player hit : hits) {
            if (!gamePhase.getGamer(hit).die()) {
                if (cantHitPlayerBecauseRespawningMsg != null) {
                    cantHitPlayerBecauseRespawningMsg.send(
                            this.shooter,
                            PlaceholderRegistry.create(gamePhase.getPlaceholders())
                                .set("shot_player", hit.getName())
                        );
                }
                continue;
            }

            hitsCount++;

            boolean headshot = location.getY() - hit.getLocation().getY() > 1.4; // Head height

            Dbg.p(String.format("[%s] Hit %s - headshot %b", shooter.getName(), hit.getName(), headshot));

            Railgun gun = shooterAccount.getRailgun();
            Message message = headshot ? headshotMessage : shotMessage;
            PlaceholderValue<String> killMessage = (gun == null || gun.getKillMessage() == null) ? defaultKillMessage : gun.getKillMessage();
            message = message.filter(
                    "killer", shooter.getName(),
                    "killed", hit.getName()
            );
            PlaceholderRegistry<?> registry = PlaceholderRegistry.create(gamePhase.getPlaceholders())
                    .set("kill_message", p -> killMessage.resolve(p, gamePhase.getPlaceholders()));

            arena.broadcast(message, registry);

            gamePhase.getGamer(shooter).onKill(headshot);

            shooterAccount.getSelectedKillSound().play(shooter.getLocation());
            shooterAccount.getSelectedKillSound().play(location);

            FireworkEffect.Type type = shooterAccount.getSelectedBarrel().getFireworkType();
            Color color = shooterAccount.getSelectedLaser().getFireworkColor();
            FireworkUtil.instantFirework( // Plays a firework in the location where the player has been killed.
                    location,
                    FireworkEffect.builder()
                            .with(type)
                            .withColor(color)
                            .build()
            );

        }

        MultiStab.tryReach(gamePhase, gamePhase.getGamer(shooter), hitsCount);

        gamePhase.goOnIfHasWon(shooter);
    }

    @Override
    public void run() {
        double speedPerTick = speed / 20.0;
        double maxDistPerTick = speedPerTick;

        // The step that should be done within this tick (speedPerTick) is subdivided into many small steps
        // long as rayStep. Every rayStep intersections are checked and a particles are spawned.
        double pStep = 0;
        while (pStep < maxDistPerTick) {
            if (!location.getBlock().isPassable()) {
                Dbg.p(String.format("[%s] Canceling shot because hit a non-passable block", shooter.getName()));
                cancel();
                break;
            }

            shooterAccount.getSelectedMuzzle() // Spawns the muzzle's particles.
                    .getParticles()
                    .forEach(particle -> particle.display(location, location.getWorld().getPlayers()));

            if (!gamePhase.isEnding()) {
                // Checks if there are intersections with other players.
                List<Player> hits = location.getWorld()
                        .getNearbyEntities(location, raySize, raySize, raySize, entity -> entity instanceof Player)
                        .stream()
                        .filter(entity -> {
                            Player player = (Player) entity;
                            return player != shooter && gamePhase.isGamer(player);
                        })
                        .map(entity -> (Player) entity)
                        .collect(Collectors.toList());

                if (!hits.isEmpty()) { // If there are intersections, kills the players and halts the ray.
                    Dbg.p(String.format("[%s] Shot hit %d players, canceling it", shooter.getName(), hits.size()));
                    kill(hits);
                    cancel();
                    break;
                }
            }

            double distInc = Math.min(rayStep, maxDistPerTick - pStep);
            pStep += distInc;
            location.add(direction.normalize().multiply(distInc));
            distance += distInc;

            if (distance >= maxDistance) { // If the ray exceed its max distance halts all.
                Dbg.p(String.format("[%s] Max distance reached, canceling shot", shooter.getName()));
                cancel();
                break;
            }
        }
    }

    public static void loadConfig() {
        Config config = Quake.getConfigSection("game");

        raySize = config.getDoubleRequired("ray-size");
        maxDistance = config.getDoubleRequired("ray-max-distance");
        speed = config.getDoubleRequired("ray-speed");
        rayStep = config.getDoubleRequired("ray-step");

        defaultKillMessage = config.getMessageStrRequired("default-kill-message");
        shotMessage = config.getMessageRequired("shot-message");
        headshotMessage = config.getMessageRequired("headshot-message");
        cantHitPlayerBecauseRespawningMsg = config.getMessage("cant-hit-player-because-respawning-message");
    }
}
