package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import xyz.upperlevel.uppercore.scoreboard.Board;

import java.time.Instant;
import java.util.*;

import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;

@Data
public class PlayingPhase implements Phase, Listener {

    private final Game game;
    private final MatchPhase parent;

    private GameHotbar hotbar;

    public PlayingPhase(MatchPhase parent) {
        this.parent = parent;
        this.game = parent.getGame();

        hotbar = (GameHotbar) get().getHotbars().get("solo_quake_ingame_hotbar");
    }

    @Override
    public void onEnable(Phase previous) {
        Bukkit.getPluginManager().registerEvents(this, get());
        for (int i = 0; i < game.getPlayers().size(); i++) {
            game.getPlayers().get(i).teleport(game.getArena().getSpawns().get(i % game.getArena().getSpawns().size()));
        }
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!game.equals(get().getGameManager().getGame(e.getPlayer())))
            return;
        Player p = e.getPlayer();

        long start = System.currentTimeMillis();

        RayTrace trace = new RayTrace(p.getEyeLocation().toVector(), p.getEyeLocation().getDirection());
        List<Vector> positions = trace.traverse(150, 0.25); // todo choose distance (and accuracy)

        for (Vector position : positions) {
            Location loc = position.toLocation(p.getWorld());

            ParticleEffect.CRIT.display(0f, 0f, 0f, 0.5f, 3, loc, 100.);

            if (e.isCancelled())
                break;

            if (loc.getBlock().getType().isSolid())
                break;

            Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, 0.25, 0.25, 0.25); // todo choose radius
            for (Entity entity : entities) {
                if (entity instanceof Player && !entity.equals(p)) {
                    // -------------- ON PLAYER HIT
                    Player hit = (Player) entity;
                    hit.setHealth(0);

                    QuakePlayer shooter = QuakePlayerManager.get().getPlayer(p);

                    shooter.kills++;
                    parent.getParticipant(p).kills++;


                    //if (barrel != null && laser != null)
                    // todo FireworkUtil.detonate(e.getLocation())

                    game.broadcast(p.getName() + " shot " + hit.getName()); // todo kill message
                    break;
                }
            }
        }

        e.getPlayer().sendMessage("It took " + (System.currentTimeMillis() - start) + " to shoot the laser");
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
