package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Data;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
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
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.QuakePlayerManager;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.spigot.quakecraft.core.math.RayTrace;
import xyz.upperlevel.spigot.quakecraft.core.particle.ParticleEffect;
import xyz.upperlevel.spigot.quakecraft.events.GameQuitEvent;
import xyz.upperlevel.spigot.quakecraft.events.LaserHitEvent;
import xyz.upperlevel.spigot.quakecraft.events.LaserSpreadEvent;
import xyz.upperlevel.uppercore.gui.hotbar.HotbarSystem;
import xyz.upperlevel.uppercore.scoreboard.ScoreboardSystem;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.*;

import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;

@Data
public class PlayingPhase implements Phase, Listener {

    private final Game game;
    private final GamePhase parent;

    private PlayingHotbar hotbar;

    private void loadHotbar() {
        File file = new File("hotbars" + File.separator + "playing_solo");
        if (file.exists()) {
            hotbar = PlayingHotbar.deserialize(QuakeCraftReloaded.get(), "playing_solo", YamlConfiguration.loadConfiguration(file)::get);
        } else
            throw new InvalidParameterException("Cannot find file: \"" + file.getPath() + "\"");
    }

    public PlayingPhase(GamePhase parent) {
        this.parent = parent;
        this.game = parent.getGame();

        loadHotbar();
    }

    public void setup(Player player) {
        HotbarSystem.view(player).addHotbar(hotbar);
    }

    public void clear(Player player) {
        HotbarSystem.view(player).removeHotbar(hotbar);
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
        ParticleEffect.CRIT.display(0f, 0f, 0f, 0.5f, 3, e.getLocation(), 100.);
    }

    private final Map<Player, Shot> shots = new HashMap<>();

    public class Shot extends BukkitRunnable {
        private final Player player;
        private final List<Vector> positions;

        private int currPos;

        public Shot(Player player) {
            this.player = player;
            this.positions = new RayTrace(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection()).traverse(150, 0.25);
            currPos = 0;
        }

        public void bang() {
            runTaskTimer(QuakeCraftReloaded.get(), 0, 1);
        }

        @Override
        public void run() {
            Vector pos = positions.get(currPos++);
            Location loc = pos.toLocation(player.getWorld());
            // laser spread
            {
                LaserSpreadEvent e = new LaserSpreadEvent(PlayingPhase.this, loc, player);
                Bukkit.getPluginManager().callEvent(e);
                if (e.isCancelled()) {
                    cancel();
                    return;
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
                            return;
                        }
                    }
                }
            }
            if (currPos == positions.size())
                cancel();
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!game.equals(get().getGameManager().getGame(e.getPlayer())))
            return;
        new Shot(e.getPlayer()).bang();
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
