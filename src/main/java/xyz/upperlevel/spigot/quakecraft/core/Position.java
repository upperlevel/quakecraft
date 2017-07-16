package xyz.upperlevel.spigot.quakecraft.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position implements Cloneable, ConfigurationSerializable {

    private double x, y, z;
    private double yaw, pitch;

    public Position(Location location) {
        x = location.getX();
        y = location.getY();
        z = location.getZ();
        yaw = location.getYaw();
        pitch = location.getPitch();
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z, (float) yaw, (float) pitch);
    }

    public Block toBlock(World world) {
        return toLocation(world).getBlock();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Position) {
            Position position = (Position) object;
            return position.x == x &&
                    position.y == y &&
                    position.z == z &&
                    position.yaw == yaw &&
                    position.pitch == pitch;
        }
        return super.equals(object);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("x", x);
        data.put("y", y);
        data.put("z", z);
        data.put("yaw", yaw);
        data.put("pitch", pitch);
        return data;
    }

    public static Position deserialize(Map<String, Object> data) {
        Position position = new Position();
        position.x = (double) data.get("x");
        position.y = (double) data.get("y");
        position.z = (double) data.get("z");
        position.yaw = (double) data.get("yaw");
        position.pitch = (double) data.get("pitch");
        return position;
    }

    public static Position from(Location location) {
        return new Position(location);
    }

    public static Position from(Block block) {
        return new Position(block.getLocation());
    }
}
