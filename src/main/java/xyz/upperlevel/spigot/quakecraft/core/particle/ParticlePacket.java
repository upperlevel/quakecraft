package xyz.upperlevel.spigot.quakecraft.core.particle;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import xyz.upperlevel.spigot.quakecraft.core.particle.data.ParticleData;
import xyz.upperlevel.spigot.quakecraft.core.particle.exceptions.PacketInstantiationException;
import xyz.upperlevel.spigot.quakecraft.core.particle.exceptions.PacketSendingException;
import xyz.upperlevel.spigot.quakecraft.core.particle.exceptions.VersionIncompatibleException;
import xyz.upperlevel.uppercore.util.NmsVersion;

import javax.management.ReflectionException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Represents a particle effect packet with all attributes which is used for sending packets to the players
 * <p>
 * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
 *
 * @author DarkBlade12
 * @since 1.5
 */
public class ParticlePacket {
    private static Class<?> particlePacketClass;
    private static PacketCreator packetCreator;
    private static Method getHandle;
    private static Field playerConnection;
    private static Method sendPacket;

    private final ParticleEffect effect;
    private float offsetX;
    private final float offsetY;
    private final float offsetZ;
    private final float speed;
    private final int amount;
    private final boolean longDistance;
    private final ParticleData data;
    private Object packet;

    static {
        try {
            particlePacketClass = ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass(NmsVersion.MINOR < 7 ? "Packet63WorldParticles" : "PacketPlayOutWorldParticles");
            if (NmsVersion.MINOR < 8)
                packetCreator = oldPacketConstructor();
            else
                packetCreator = newPacketConstructor();

            getHandle = ReflectionUtils.getMethod("CraftPlayer", ReflectionUtils.PackageType.CRAFTBUKKIT_ENTITY, "getHandle");
            playerConnection = ReflectionUtils.getField("EntityPlayer", ReflectionUtils.PackageType.MINECRAFT_SERVER, false, "playerConnection");
            sendPacket = ReflectionUtils.getMethod(playerConnection.getType(), "sendPacket", ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("Packet"));
        } catch (Exception exception) {
            throw new VersionIncompatibleException("Your current bukkit version seems to be incompatible with this library", exception);
        }
    }

    /**
     * Construct a new particle packet
     *
     * @param effect Particle effect
     * @param offsetX Maximum distance particles can fly away from the center on the x-axis
     * @param offsetY Maximum distance particles can fly away from the center on the y-axis
     * @param offsetZ Maximum distance particles can fly away from the center on the z-axis
     * @param speed Display speed of the particles
     * @param amount Amount of particles
     * @param longDistance Indicates whether the maximum distance is increased from 256 to 65536
     * @param data Data of the effect
     * @throws IllegalArgumentException If the speed or amount is lower than 0
     * @see #initialize()
     */
    public ParticlePacket(ParticleEffect effect, float offsetX, float offsetY, float offsetZ, float speed, int amount, boolean longDistance, ParticleData data) throws IllegalArgumentException {
        if (speed < 0) {
            throw new IllegalArgumentException("The speed is lower than 0");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("The amount is lower than 0");
        }
        this.effect = effect;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.speed = speed;
        this.amount = amount;
        this.longDistance = longDistance;
        this.data = data;
    }

    /**
     * Construct a new particle packet of a single particle flying into a determined direction
     *
     * @param effect Particle effect
     * @param direction Direction of the particle
     * @param speed Display speed of the particle
     * @param longDistance Indicates whether the maximum distance is increased from 256 to 65536
     * @param data Data of the effect
     * @throws IllegalArgumentException If the speed is lower than 0
     */
    public ParticlePacket(ParticleEffect effect, Vector direction, float speed, boolean longDistance, ParticleData data) throws IllegalArgumentException {
        this(effect, (float) direction.getX(), (float) direction.getY(), (float) direction.getZ(), speed, 0, longDistance, data);
    }

    /**
     * Construct a new particle packet of a single colored particle
     *
     * @param effect Particle effect
     * @param color Color of the particle
     * @param longDistance Indicates whether the maximum distance is increased from 256 to 65536
     */
    public ParticlePacket(ParticleEffect effect, ParticleColor color, boolean longDistance) {
        this(effect, color.valueX, color.valueY, color.valueZ, 1, 0, longDistance, null);
        if (effect == ParticleEffect.REDSTONE && color.valueX == 0)
            offsetX = Float.MIN_NORMAL;
    }

    /**
     * Initializes {@link #packet} with all set values
     *
     * @param center Center location of the effect
     * @throws PacketInstantiationException If instantion fails due to an unknown error
     */
    private void initializePacket(Location center) throws PacketInstantiationException {
        if (packet != null) {
            return;
        }
        try {
            packet = packetCreator.create(effect, data, longDistance, (float)center.getX(), (float)center.getY(), (float)center.getZ(), offsetX, offsetY, offsetZ, speed, amount);
        } catch (Exception exception) {
            throw new PacketInstantiationException("Packet instantiation failed", exception);
        }
    }

    /**
     * Sends the packet to a single player and caches it
     *
     * @param center Center location of the effect
     * @param player Receiver of the packet
     * @throws PacketInstantiationException If instantion fails due to an unknown error
     * @throws PacketSendingException If sending fails due to an unknown error
     * @see #initializePacket(Location)
     */
    public void sendTo(Location center, Player player) throws PacketInstantiationException, PacketSendingException {
        initializePacket(center);
        try {
            sendPacket.invoke(playerConnection.get(getHandle.invoke(player)), packet);
        } catch (Exception exception) {
            throw new PacketSendingException("Failed to send the packet to player '" + player.getName() + "'", exception);
        }
    }

    /**
     * Sends the packet to all players in the list
     *
     * @param center Center location of the effect
     * @param players Receivers of the packet
     * @throws IllegalArgumentException If the player list is empty
     * @see #sendTo(Location center, Player player)
     */
    public void sendTo(Location center, Iterable<Player> players) throws IllegalArgumentException {
        for (Player player : players)
            sendTo(center, player);
    }

    /**
     * Sends the packet to all players in the list
     *
     * @param center Center location of the effect
     * @param players Receivers of the packet
     * @throws IllegalArgumentException If the player list is empty
     * @see #sendTo(Location center, Player player)
     */
    public void sendTo(Location center, Stream<Player> players) throws IllegalArgumentException {
        players.forEach(p -> sendTo(center, players));
    }

    /**
     * Sends the packet to all players in a certain range
     *
     * @param center Center location of the effect
     * @param range Range in which players will receive the packet (Maximum range for particles is usually 16, but it can differ for some types)
     * @throws IllegalArgumentException If the range is lower than 1
     * @see #sendTo(Location center, Player player)
     */
    public void sendTo(Location center, double range) throws IllegalArgumentException {
        if (range < 1.0)
            throw new IllegalArgumentException("The range is lower than 1");
        forEveryoneAround(center, range, p -> sendTo(center, p));
    }

    //TODO: maybe if we check the range as a square it's faster
    private void forEveryoneAround(Location center, double radius, Consumer<Player> action) {
        int chunkRadius = (int) Math.ceil(radius) >> 4;
        double squared = radius * radius;
        final int x = center.getBlockX() >> 4;
        final int z = center.getBlockZ() >> 4;

        int ix = x - chunkRadius;
        int ex = x + chunkRadius;

        int iz = z - chunkRadius;
        int ez = z + chunkRadius;

        final World world = center.getWorld();
        for (int chX = ix; chX <= ex; chX++) {
            for (int chZ = iz; chZ <= ez; chZ++) {
                if(world.isChunkLoaded(chX, chZ)) {
                    for (Entity e : world.getChunkAt(chX, chZ).getEntities()) {
                        if (e instanceof Player && e.getLocation().distanceSquared(center) <= squared) {
                            action.accept((Player) e);
                        }
                    }
                }
            }
        }
    }

    /*
            Class<?> packetClass = ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass(NmsVersion.MINOR < 7 ? "Packet63WorldParticles" : "PacketPlayOutWorldParticles");
            Constructor packetConstructor = ReflectionUtils.getConstructor(packetClass);
            packet = packetConstructor.newInstance();
            if (NmsVersion.MINOR < 8) {
                String name = effect.getName();
                if (data != null) {
                    name += data.getPacketDataString();
                }
                ReflectionUtils.setValue(packet, true, "a", name);
            } else {
                ReflectionUtils.setValue(packet, true, "a", ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("EnumParticle").getEnumConstants()[effect.getId()]);
                ReflectionUtils.setValue(packet, true, "j", longDistance);
                if (data != null) {
                    int[] packetData = data.getPacketData();
                    ReflectionUtils.setValue(packet, true, "k", effect == ParticleEffect.ITEM_CRACK ? packetData : new int[] { packetData[0] | (packetData[1] << 12) });
                }
            }
            ReflectionUtils.setValue(packet, true, "b", (float) center.getX());
            ReflectionUtils.setValue(packet, true, "c", (float) center.getY());
            ReflectionUtils.setValue(packet, true, "d", (float) center.getZ());
            ReflectionUtils.setValue(packet, true, "e", offsetX);
            ReflectionUtils.setValue(packet, true, "f", offsetY);
            ReflectionUtils.setValue(packet, true, "g", offsetZ);
            ReflectionUtils.setValue(packet, true, "h", speed);
            ReflectionUtils.setValue(packet, true, "i", amount);
     */

    private interface PacketCreator {
        Object create(ParticleEffect effect, ParticleData data, boolean longDistance, float posX, float posY, float posZ, float offsetX, float offsetY, float offsetZ, float speed, int amount) throws Exception;
    }

    private static PacketCreator oldPacketConstructor() throws NoSuchMethodException {
        final Constructor<?> constructor = particlePacketClass.getConstructor(
                String.class,
                Float.TYPE, Float.TYPE, Float.TYPE,
                Float.TYPE, Float.TYPE, Float.TYPE,
                Float.TYPE,
                Integer.TYPE
        );
        return (effect, data, longDistance, posX, posY, posZ, offsetX, offsetY, offsetZ, speed, amount) -> {
            String name = effect.getName();
            if(data != null)
                name += data.getPacketDataString();
            return constructor.newInstance(
                    name,
                    posX, posY, posZ,
                    offsetX, offsetY, offsetZ,
                    speed,
                    amount
            );
        };
    }

    private static PacketCreator newPacketConstructor() throws NoSuchMethodException, ClassNotFoundException {
        Class<?> enumParticle = ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("EnumParticle");
        Object[] particleValues = enumParticle.getEnumConstants();
        final Constructor<?> constructor = particlePacketClass.getConstructor(
                enumParticle,
                Boolean.TYPE,
                Float.TYPE, Float.TYPE, Float.TYPE,
                Float.TYPE, Float.TYPE, Float.TYPE,
                Float.TYPE,
                Integer.TYPE,
                int[].class
        );
        return (effect, data, longDistance, posX, posY, posZ, offsetX, offsetY, offsetZ, speed, amount) -> {
            int[] rawData;
            if (data != null) {
                int[] packetData = data.getPacketData();
                rawData = effect == ParticleEffect.ITEM_CRACK ? packetData : new int[] { packetData[0] | (packetData[1] << 12) };
            } else rawData = null;


            return constructor.newInstance(
                    particleValues[effect.getId()],
                    longDistance,
                    posX, posY, posZ,
                    offsetX, offsetY, offsetZ,
                    speed,
                    amount,
                    rawData
            );
        };
    }
}