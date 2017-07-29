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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
    private static Class<?> enumParticle;
    private static Constructor<?> packetConstructor;
    private static Method getHandle;
    private static Field playerConnection;
    private static Method sendPacket;
    private static boolean initialized;
    private final ParticleEffect effect;
    private float offsetX;
    private final float offsetY;
    private final float offsetZ;
    private final float speed;
    private final int amount;
    private final boolean longDistance;
    private final ParticleData data;
    private Object packet;

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
        initialize();//TODO maybe it's better to move this
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
     * Initializes {@link #packetConstructor}, {@link #getHandle}, {@link #playerConnection} and {@link #sendPacket} and sets {@link #initialized} to <code>true</code> if it succeeds
     * <p>
     * <b>Note:</b> These fields only have to be initialized once, so it will return if {@link #initialized} is already set to <code>true</code>
     *
     * @throws VersionIncompatibleException if your bukkit version is not supported by this library
     */
    public static void initialize() throws VersionIncompatibleException {
        if (initialized)
            return;
        try {
            if (NmsVersion.MINOR > 7) {
                enumParticle = ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("EnumParticle");
            }
            Class<?> packetClass = ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass(NmsVersion.MINOR < 7 ? "Packet63WorldParticles" : "PacketPlayOutWorldParticles");
            packetConstructor = ReflectionUtils.getConstructor(packetClass);
            getHandle = ReflectionUtils.getMethod("CraftPlayer", ReflectionUtils.PackageType.CRAFTBUKKIT_ENTITY, "getHandle");
            playerConnection = ReflectionUtils.getField("EntityPlayer", ReflectionUtils.PackageType.MINECRAFT_SERVER, false, "playerConnection");
            sendPacket = ReflectionUtils.getMethod(playerConnection.getType(), "sendPacket", ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("Packet"));
        } catch (Exception exception) {
            throw new VersionIncompatibleException("Your current bukkit version seems to be incompatible with this library", exception);
        }
        initialized = true;
    }

    /**
     * Determine if {@link #packetConstructor}, {@link #getHandle}, {@link #playerConnection} and {@link #sendPacket} are initialized
     *
     * @return Whether these fields are initialized or not
     * @see #initialize()
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Initializes {@link #packet} with all set values
     *
     * @param center Center location of the effect
     * @throws PacketInstantiationException If instantion fails due to an unknown error
     */
    private void initializePacket(Location center) throws PacketInstantiationException {
        //TODO please cache the reflections
        if (packet != null) {
            return;
        }
        try {
            packet = packetConstructor.newInstance();
            if (NmsVersion.MINOR < 8) {
                String name = effect.getName();
                if (data != null) {
                    name += data.getPacketDataString();
                }
                ReflectionUtils.setValue(packet, true, "a", name);
            } else {
                ReflectionUtils.setValue(packet, true, "a", enumParticle.getEnumConstants()[effect.getId()]);
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
}