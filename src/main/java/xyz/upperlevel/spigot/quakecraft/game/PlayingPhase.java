package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Data;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.QuakePlayerManager;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.spigot.quakecraft.core.math.RayTrace;
import xyz.upperlevel.spigot.quakecraft.events.GameQuitEvent;
import xyz.upperlevel.spigot.quakecraft.events.LaserHitEvent;
import xyz.upperlevel.spigot.quakecraft.events.LaserSpreadEvent;
import xyz.upperlevel.uppercore.hotbar.HotbarManager;

import java.io.File;
import java.util.*;

import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;
import static xyz.upperlevel.uppercore.Uppercore.hotbars;

@Data
public class PlayingPhase implements Phase, Listener {

    private final Game game;
    private final GamePhase parent;

    private static final PlayingHotbar hotbar;

    static {
        File f = new File(get().getHotbars().getFolder(), "playing_solo.yml");
        if (!f.exists())
            throw new IllegalArgumentException("Cannot find file: \"" + f.getPath() + "\"");
        hotbar = PlayingHotbar.deserialize(get(), "playing_solo", YamlConfiguration.loadConfiguration(f)::get);
    }

    public PlayingPhase(GamePhase parent) {
        this.parent = parent;
        this.game = parent.getGame();
    }

    public void setup(Player player) {
        hotbars().view(player).addHotbar(hotbar);
    }

    public void clear(Player player) {
        hotbars().view(player).removeHotbar(hotbar);
    }

    public void clear() {
        for (Player p : game.getPlayers())
            clear(p);
    }

    @Override
    public void onEnable(Phase previous) {
        Bukkit.getPluginManager().registerEvents(this, get());
        List<Player> pList = new ArrayList<>(game.getPlayers());
        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player p = pList.get(i);
            setup(p);
            p.teleport(game.getArena().getSpawns().get(i % game.getArena().getSpawns().size()));
        }
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);
        clear();
    }

    @EventHandler
    public void onGameQuit(GameQuitEvent e) {
        clear(e.getPlayer());
    }

    @EventHandler
    public void onLaserHit(LaserHitEvent e) {
        e.getHit().setHealth(0);
        game.broadcast(e.getShooter().getName() + " shot " + e.getHit().getName()); // todo kill message
    }

    @EventHandler
    public void onLaserSpread(LaserSpreadEvent e) {
        e.getLocation().getWorld().spawnParticle(Particle.DRIP_LAVA, e.getLocation(), 25);
    }


    public static final double LASER_BLOCKS_PER_TICK = 5;
    public static final double LASER_BLOCKS_INTERVAL = 0.25;

    private final Map<Player, Shot> shots = new HashMap<>();

    public class Shot extends BukkitRunnable {
        private final Player player;
        private final List<Vector> positions;

        private int positionIndex;

        public Shot(Player player) {
            this.player = player;
            this.positions = new RayTrace(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection()).traverse(150, 0.25);
            positionIndex = 0;
        }

        public void bang() {
            runTaskTimer(QuakeCraftReloaded.get(), 0, 1);
        }

        @Override
        public void run() {
            for (double distance = 0; distance < LASER_BLOCKS_PER_TICK; distance += LASER_BLOCKS_INTERVAL) {
                Vector pos = positions.get(positionIndex++);
                Location loc = pos.toLocation(player.getWorld());
                // laser spread
                {
                    LaserSpreadEvent e = new LaserSpreadEvent(PlayingPhase.this, loc, player);
                    Bukkit.getPluginManager().callEvent(e);
                    if (e.isCancelled()) {
                        cancel();
                        break;
                    }
                }
                // laser hit
                Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, 0.25, 0.25, 0.25); // todo choose radius
                for (Entity entity : entities) {
                    if (entity instanceof Player && !entity.equals(player)) {
                        Player hit = (Player) entity;
                        {
                            LaserHitEvent e = new LaserHitEvent(PlayingPhase.this, loc, player, hit);
                            Bukkit.getPluginManager().callEvent(e);
                            if (e.isCancelled()) {
                                cancel();
                                break;
                            }
                        }
                    }
                }
                // laser hit block
                if (loc.getBlock().getType().isSolid()) {
                    cancel();
                    break;
                }
                if (positionIndex == positions.size()) {
                    cancel();
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!game.equals(get().getGameManager().getGame(p)))
            return;
        if (p.getInventory().getHeldItemSlot() == get().getConfig().getInt("playing-hotbar.gun.slot"))
            new Shot(p).bang();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        if (!game.equals(get().getGameManager().getGame(player)))
            return;

        QuakePlayerManager.get().getPlayer(player).deaths++;
        parent.getParticipant(player).deaths++;

        e.getEntity().spigot().respawn();
        e.setDeathMessage(null);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        List<Location> spawns = game.getArena().getSpawns();
        e.setRespawnLocation(spawns.get(new Random().nextInt(spawns.size())));
    }
}
