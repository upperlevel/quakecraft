package xyz.upperlevel.quakecraft.powerup;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.arena.Arena;
import xyz.upperlevel.quakecraft.events.PowerupPickupEvent;
import xyz.upperlevel.quakecraft.game.GamePhase;
import xyz.upperlevel.quakecraft.game.Participant;
import xyz.upperlevel.quakecraft.powerup.effects.PowerupEffect;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;
import xyz.upperlevel.uppercore.util.LocUtil;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Powerup {
    private static final double SPAWN_HEIGHT = 1.0;
    private static Map<Item, Powerup> drops = new HashMap<>();

    private final Arena arena;
    private Location location;
    private PowerupEffect effect;
    private int respawnTicks;

    private Item spawned;
    private GamePhase phase;
    private BukkitTask spawner;

    public Powerup(Arena arena, Location location, PowerupEffect effect, int respawnTicks) {
        this.arena = arena;
        this.location = location;
        this.effect = effect;
        this.respawnTicks = respawnTicks;
    }

    public Powerup(Arena arena, Config config) {
        this.arena = arena;
        load(config);
    }

    public void spawn() {
        spawner = null;
        spawned = location.getWorld().dropItem(location.clone().add(0.0, SPAWN_HEIGHT, 0.0), effect.getDisplay());
        spawned.setVelocity(new Vector());
        spawned.setGravity(false);
        drops.put(spawned, this);
    }

    private void onPickup(Participant player) {
        effect.apply(player);
        spawned.remove();
        spawned = null;
        beginSpawnTask();
    }

    public void onGameBegin(GamePhase phase) {
        beginSpawnTask();
        this.phase = phase;
    }

    public void onGameEnd() {
        if(spawned != null) {
            spawned.remove();
        } else {
            spawner.cancel();
        }
    }

    protected void beginSpawnTask() {
        spawner = Bukkit.getScheduler().runTaskLater(
                Quakecraft.get(),
                this::spawn,
                respawnTicks
        );
    }

    public Map<String, Object> save() {
        return ImmutableMap.of(
                "loc", LocUtil.serialize(location),
                "effect", effect.getId(),
                "respawn-ticks", respawnTicks
        );
    }

    public void load(Config config) {
        this.location = LocUtil.deserialize(config.getConfigRequired("loc"));
        String effectId = config.getStringRequired("effect");
        this.effect = PowerupEffectManager.fromId(effectId);
        if(effect == null)
            throw new InvalidConfigurationException("Invalid powerup effect '" + effectId + "'");
        this.respawnTicks = config.getIntRequired("respawn-ticks");
    }

    public static class EventListener implements Listener {
        @EventHandler
        public void onPlayerPickup(PlayerPickupItemEvent event) {
            Powerup box = drops.remove(event.getItem());
            if(box != null) {
                event.setCancelled(true);
                Participant p = box.phase.getParticipant(event.getPlayer());
                if(p == null) {
                    drops.put(event.getItem(), box);
                    return;
                }
                PowerupPickupEvent e = new PowerupPickupEvent(box, p);
                if(!e.isCancelled())
                    box.onPickup(p);
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onItemMerge(ItemMergeEvent event) {
            if(     drops.containsKey(event.getEntity()) ||
                    drops.containsKey(event.getTarget()))
                event.setCancelled(true);
        }

        @EventHandler(ignoreCancelled = true)
        public void onItemDespawnEvent(ItemDespawnEvent event) {
            if(drops.containsKey(event.getEntity()))
                event.setCancelled(true);
        }
    }

    public static void load() {
        Bukkit.getPluginManager().registerEvents(new EventListener(), Quakecraft.get());
    }
}
