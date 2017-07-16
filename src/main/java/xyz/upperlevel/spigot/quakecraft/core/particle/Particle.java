package xyz.upperlevel.spigot.quakecraft.core.particle;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

@Data
public abstract class Particle {

    private final ParticleType type;

    private float offsetX, offsetY, offsetZ;
    private float speed;
    private int amount;
    private double range;

    public Particle(ParticleType type) {
        this.type = type;

        setOffset(0f,0f,0f);
        setSpeed(0f);
        setAmount(0);
        setRange(0.0);
    }

    public Particle(ParticleType type, Map<String, Object> data) {
        this.type = type;

        setOffset(
                (Float) data.get("offset.x"),
                (Float) data.get("offset.y"),
                (Float) data.get("offset.z")
        );
        setSpeed((Float) data.get("speed"));
        setAmount((Integer) data.get("amount"));
        setRange((Double) data.get("range"));
    }

    public void setOffset(Float x, Float y, Float z) {
        offsetX = x == null ? 0 : x;
        offsetY = y == null ? 0 : y;
        offsetZ = z == null ? 0 : z;
    }

    public void setSpeed(Float speed) {
        this.speed = speed == null || speed <= 0 ? 0 : speed;
    }

    public void setAmount(Integer amount) {
        this.amount = amount == null || amount <= 0 ? 0 : amount;
    }

    public void setRange(Double range) {
        this.range = Math.abs(range == null ? 0 : range);
    }

    public abstract void display(Location location);
}
