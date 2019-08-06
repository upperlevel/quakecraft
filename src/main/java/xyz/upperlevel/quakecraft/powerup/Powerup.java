package xyz.upperlevel.quakecraft.powerup;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.quakecraft.events.PowerupPickupEvent;
import xyz.upperlevel.quakecraft.phases.GamePhase;
import xyz.upperlevel.quakecraft.phases.Gamer;
import xyz.upperlevel.quakecraft.powerup.effects.PowerupEffect;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.ConfigConstructor;
import xyz.upperlevel.uppercore.config.ConfigProperty;
import xyz.upperlevel.uppercore.nms.NmsVersion;
import xyz.upperlevel.uppercore.nms.impl.TagNms;
import xyz.upperlevel.uppercore.nms.impl.entity.EntityNms;
import xyz.upperlevel.uppercore.util.ItemDemerger;
import xyz.upperlevel.uppercore.util.LocUtil;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Powerup {
    private static final double SPAWN_HEIGHT = 1.0;
    private static Map<Item, Powerup> drops = new HashMap<>();

    private Location location;
    private PowerupEffect effect;
    private int respawnTicks;

    private Item spawned;
    //Only used in versions < 1.10 to make the Powerup float
    private ArmorStand support = null;

    private GamePhase phase;
    private BukkitTask spawner;

    @ConfigConstructor
    public Powerup(
            @ConfigProperty("loc") Location location,
            @ConfigProperty("effect") String effectId,
            @ConfigProperty("respawn-ticks") int respawnTicks) {
        this(location, PowerupEffectManager.fromId(effectId), respawnTicks);
    }

    public Powerup(Location location, PowerupEffect effect, int respawnTicks) {
        this.location = location;
        this.effect = effect;
        this.respawnTicks = respawnTicks;
    }

    public void spawn() {
        spawner = null;
        ItemStack display = effect.getDisplay();
        display = ItemDemerger.setItem(display);
        Location spawnLoc = location.clone().add(0.0, SPAWN_HEIGHT, 0.0);
        spawned = location.getWorld().dropItem(spawnLoc, display);
        spawned.setVelocity(new Vector());
        if (NmsVersion.MINOR >= 10) {
            spawned.setGravity(false);
        } else {
            if (NmsVersion.RELEASE == 1) {
                //Markers NOT supported
                spawnLoc = spawnLoc.subtract(0.0, 1.975, 0.0);
            }
            support = location.getWorld().spawn(spawnLoc, ArmorStand.class);
            EntityNms.editTag(support, tag -> {
                TagNms.setBoolean(tag, "Invisible", true);
                TagNms.setBoolean(tag, "Marker", true);
                TagNms.setBoolean(tag, "NoGravity", true);
            });
            support.setPassenger(spawned);
        }
        drops.put(spawned, this);
    }

    private void onPickup(Gamer player) {
        effect.apply(player);
        spawned.remove();
        spawned = null;
        if (support != null) {
            support.remove();
            support = null;
        }
        beginSpawnTask();
    }

    public void onGameBegin(GamePhase phase) {
        beginSpawnTask();
        this.phase = phase;
    }

    public void onGameEnd() {
        if (spawned != null) {
            spawned.remove();
        } else {
            spawner.cancel();
        }
    }

    protected void beginSpawnTask() {
        spawner = Bukkit.getScheduler().runTaskLater(
                Quake.get(),
                this::spawn,
                respawnTicks
        );
    }

    public Map<String, Object> serialize() {
        return ImmutableMap.of(
                "loc", LocUtil.serialize(location),
                "effect", effect.getId(),
                "respawn-ticks", respawnTicks
        );
    }

    public void load(Config config) {
        this.location = LocUtil.deserialize(config.getConfigRequired("loc"));
        this.effect = config.getRequired("effect", PowerupEffect.class);
        this.respawnTicks = config.getIntRequired("respawn-ticks");
    }

    public static class EventListener implements Listener {
        @EventHandler
        public void onPlayerPickup(PlayerPickupItemEvent event) {
            Powerup box = drops.remove(event.getItem());
            if (box != null) {
                event.setCancelled(true);
                Gamer p = box.phase.getGamer(event.getPlayer());
                if (p == null) {
                    drops.put(event.getItem(), box);
                    return;
                }
                PowerupPickupEvent e = new PowerupPickupEvent(box, p);
                if (!e.isCancelled())
                    box.onPickup(p);
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onItemDespawnEvent(ItemDespawnEvent event) {
            if (drops.containsKey(event.getEntity()))
                event.setCancelled(true);
        }
    }

    public static void load() {
        Bukkit.getPluginManager().registerEvents(new EventListener(), Quake.get());
    }
}
