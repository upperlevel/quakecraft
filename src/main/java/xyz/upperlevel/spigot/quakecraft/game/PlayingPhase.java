package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Data;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.QuakePlayerManager;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.spigot.quakecraft.core.PlayerUtil;
import xyz.upperlevel.spigot.quakecraft.core.math.RayTrace;
import xyz.upperlevel.spigot.quakecraft.events.*;
import xyz.upperlevel.uppercore.hotbar.HotbarManager;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.bukkit.ChatColor.RED;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;
import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;
import static xyz.upperlevel.uppercore.Uppercore.hotbars;

@Data
public class PlayingPhase implements Phase, Listener {
    public static final int SECONDS_TO_TICKS = 20;

    private final Game game;
    private final GamePhase parent;

    private static final PlayingHotbar hotbar;

    private Set<Player> dashing = new HashSet<>();
    private float defDashPower = 2f;

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
        if (p.getInventory().getHeldItemSlot() == get().getConfig().getInt("playing-hotbar.gun.slot")) {//TODO parse before game :(
            if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
                new Shot(p).bang();
            else if(e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                dash(p);
            }
        }
    }

    private void dash(Player p) {
        if(dashing.contains(p)) {
            p.sendMessage(RED + "Dash cooling down");
            return;
        }

        QuakePlayer player = get().getPlayerManager().getPlayer(p);
        float power = player.getSelectedDashPower().getPower();
        float cooldown = player.getSelectedDashCooldown().getCooldown();

        PlayerDashEvent event = new PlayerDashEvent(player, power, cooldown);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled())
            return;
        power = event.getPower();
        cooldown = event.getCooldown();

       p.setVelocity(p.getLocation().getDirection().multiply(power * defDashPower));
        dashing.add(p);
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getPluginManager().callEvent(new PlayerDashCooldownEnd(player));
                dashing.remove(p);
            }
        }.runTaskLater(get(), (int)cooldown*SECONDS_TO_TICKS);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        QuakePlayerManager.get().getPlayer(e.getEntity()).deaths++;
        parent.getParticipant(e.getEntity()).deaths++;
        e.setDeathMessage(null);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        List<Location> spawns = game.getArena().getSpawns();
        e.setRespawnLocation(spawns.get(new Random().nextInt(spawns.size())));
    }
}
