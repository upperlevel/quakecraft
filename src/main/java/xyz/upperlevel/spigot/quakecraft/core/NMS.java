package xyz.upperlevel.spigot.quakecraft.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;


public final class NMS {

    private static final String version;

    private NMS() {
    }

    static {
        String path = Bukkit.getServer().getClass().getPackage().getName();
        version = path.substring(path.lastIndexOf(".") + 1, path.length());
    }

    public static Class<?> getNMSClass(String relative) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + version + "." + relative);
    }

    public static void setNoAI(Entity entity, boolean value) {
        setCompound(entity, "NoAI", value);
    }

    public static void setSilent(Entity entity, boolean value) {
        setCompound(entity, "Silent", value);
    }

    public static void setInvulnerable(Entity entity, boolean value) {
        setCompound(entity, "Invulnerable", value);
    }

    public static void freeze(Entity entity) {
        setNoAI(entity, true);
        setSilent(entity, true);
        setInvulnerable(entity, true);
    }

    public static void setCompound(Entity entity, String name, boolean value) {
        try {
            Object entityHandle = entity.getClass().getDeclaredMethod("getHandle").invoke(entity);
            Class<?> compoundClass = getNMSClass("NBTTagCompound");
            Object compound = compoundClass.getDeclaredConstructor().newInstance();
            entityHandle.getClass()
                    .getDeclaredMethod("c", compoundClass)
                    .invoke(entityHandle, compound);
            compoundClass
                    .getDeclaredMethod("setByte", String.class, byte.class)
                    .invoke(compound, name, value ? (byte) 1 : 0);
            entityHandle.getClass()
                    .getDeclaredMethod("f", compoundClass)
                    .invoke(entityHandle, compound);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
