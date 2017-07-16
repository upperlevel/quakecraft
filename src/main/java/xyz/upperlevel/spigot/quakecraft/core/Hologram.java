package xyz.upperlevel.spigot.quakecraft.core;

import lombok.Data;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.LinkedList;
import java.util.List;

@Data
public class Hologram {

    public static final double LINE_HEIGHT = 0.25;

    private Location location;

    private final List<String> lines = new LinkedList<>();
    private final List<ArmorStand> entities = new LinkedList<>();

    private boolean spawned = false;

    public Hologram(Location location) {
        this.location = location;
    }

    public void spawn() {
        Chunk cnk = location.getChunk();
        if (!cnk.isLoaded())
            cnk.load();
        for (int i = 0; i < lines.size(); i++) {
            Location spawn = location.clone().subtract(0, i * LINE_HEIGHT, 0);
            ArmorStand entity = (ArmorStand) location.getWorld().spawnEntity(spawn, EntityType.ARMOR_STAND);
            entity.setGravity(false);
            entity.setBasePlate(false);
            entity.setVisible(false);
            entity.setCustomNameVisible(true);
            entity.setCustomName(lines.get(i));
            entities.add(entity);
        }
        spawned = true;
    }

    public void update() {
        remove();
        spawn();
    }

    public void destroy() {
        remove();
        lines.clear();
    }

    public ArmorStand get(int index) {
        return entities.get(index);
    }

    public Hologram add(String line) {
        lines.add(line);
        if (spawned)
            update();
        return this;
    }

    public Hologram set(int index, String line) {
        lines.set(index, line);
        if (spawned)
            update();
        return this;
    }

    private boolean removeEntity(ArmorStand entity) {
        lines.remove(entity.getCustomName());
        entity.remove();
        boolean removed = entities.remove(entity);
        if (removed && spawned)
            update();
        return removed;
    }

    public boolean remove(String line) {
        for (ArmorStand entity : entities)
            if (entity.getCustomName().equals(line))
                return removeEntity(entity);
        return false;
    }

    public void remove() {
        entities.forEach(Entity::remove);
        entities.clear();
    }
}
