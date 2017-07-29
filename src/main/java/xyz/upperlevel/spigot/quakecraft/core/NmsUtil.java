package xyz.upperlevel.spigot.quakecraft.core;

import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import static xyz.upperlevel.uppercore.util.NmsVersion.VERSION;


public final class NmsUtil {
    //-----------------setCompound
    private static final Method entityGetHandle;
    private static final Constructor<?> nbtTagCompoundConstructor;
    private static final Method entityGetTag;
    private static final Method entitySetTag;
    private static final Method tagSetByte;
    //-----------------instantFirework
    private static final Field fireworkExpectedLifespan;
    private static final Field fireworkTicksFlown;

    static {
        try {
            //-----------------setCompound
            entityGetHandle = getCraftClass("entity.CraftEntity").getDeclaredMethod("getHandle");
            Class<?> nbtTagCompoundClass = getNMSClass("NBTTagCompound");
            nbtTagCompoundConstructor = nbtTagCompoundClass.getConstructor();
            final Class<?> nmsEntity = getNMSClass("Entity");
            entityGetTag = nmsEntity.getDeclaredMethod("c", nbtTagCompoundClass);
            entitySetTag = nmsEntity.getDeclaredMethod("f", nbtTagCompoundClass);
            tagSetByte = nbtTagCompoundClass.getDeclaredMethod("setByte", String.class, byte.class);
            //-----------------instantFirework
            Class<?> fireworkClass = getNMSClass("EntityFireworks");
            fireworkExpectedLifespan = fireworkClass.getDeclaredField("expectedLifespan");
            fireworkTicksFlown = fireworkClass.getDeclaredField("ticksFlown");
            fireworkTicksFlown.setAccessible(true);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private NmsUtil() {
    }

    public static Class<?> getNMSClass(String relative) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + VERSION + "." + relative);
    }

    public static Class<?> getCraftClass(String relative) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + VERSION + "." + relative);
    }

    public static void setNoAI(Entity entity, boolean value) {
        setCompound(entity, tag -> setTag(tag, "NoAI", value));
    }

    public static void setSilent(Entity entity, boolean value) {
        setCompound(entity, tag -> setTag(tag, "Silent", value));
    }

    public static void setInvulnerable(Entity entity, boolean value) {
        setCompound(entity, tag -> setTag(tag, "Invulnerable", value));
    }

    public static void freeze(Entity entity, boolean value) {
        setCompound(entity, tag -> {
            setTag(tag, "NoAI", value);
            setTag(tag, "Silent", value);
            setTag(tag, "Invulnerable", value);
        });
    }

    public static void setCompound(Entity entity, Consumer<Object> tagConsumer) {
        try {
            Object handle = entityGetHandle.invoke(entity);
            Object compound = nbtTagCompoundConstructor.newInstance();
            entityGetTag.invoke(handle, compound);
            tagConsumer.accept(compound);
            entitySetTag.invoke(handle, compound);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static void setTag(Object tag, String name, boolean value) {
        try {
            tagSetByte.invoke(tag, name, (byte)(value ? 1 : 0));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static void instantFirework(Firework firework) {
        /*
        EntityFirework handle = ((CraftFirework)firework).getHandle();
        handle.ticksFlown = handle.expectedLifespan - 1;
         */
        try {
            Object handle = entityGetHandle.invoke(firework);
            fireworkTicksFlown.setInt(handle, fireworkExpectedLifespan.getInt(handle) - 1);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static void instantFirework(Location loc, FireworkEffect effect) {
        Firework firework = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(effect);
        firework.setFireworkMeta(meta);
        instantFirework(firework);
    }
}
