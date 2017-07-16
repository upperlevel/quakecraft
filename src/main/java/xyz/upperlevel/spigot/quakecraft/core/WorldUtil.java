package xyz.upperlevel.spigot.quakecraft.core;

import org.bukkit.*;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class WorldUtil {

    private WorldUtil() {
    }

    public static void playSound(Location location, Sound sound) {
        location.getWorld().playSound(location, sound, 0f, 100f);
    }


    public static boolean isFilenameValid(String name) {
        File file = new File(name);
        try {
            file.getCanonicalPath();
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    public static World createEmptyWorld(String name) {
        WorldCreator worldCreator = new WorldCreator(name);
        worldCreator.environment(World.Environment.NORMAL);
        worldCreator.generateStructures(false);
        worldCreator.generator(new ChunkGenerator() {

            @Override
            public List<BlockPopulator> getDefaultPopulators(World world) {
                return Arrays.asList(new BlockPopulator[0]);
            }

            @Override
            public boolean canSpawn(World world, int x, int z) {
                return true;
            }

            @SuppressWarnings("deprecation")
            @Override
            public byte[] generate(World world, Random random, int x, int z) {
                return new byte[32768];
            }
        });

        World world = worldCreator.createWorld();
        world.setDifficulty(Difficulty.NORMAL);
        world.setSpawnFlags(true, true);
        world.setPVP(true);
        world.setStorm(false);
        world.setThundering(false);
        world.setWeatherDuration(Integer.MAX_VALUE);
        world.setAutoSave(false);
        world.setKeepSpawnInMemory(false);
        world.setTicksPerAnimalSpawns(1);
        world.setTicksPerMonsterSpawns(1);

        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("mobGriefing", "false");
        world.setGameRuleValue("doFireTick", "false");
        world.setGameRuleValue("showDeathMessages", "false");

        return world;
    }

    public static void unloadWorld(World world) {
        Bukkit.getServer().unloadWorld(world, true);
    }

    public static void deleteWorld(World world) {
        deleteFolder(world.getWorldFolder());
    }

    public static boolean deleteFolder(File path) {
        if (path.exists()) {
            File files[] = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteFolder(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return path.delete();
    }

    private static final List<String> toIgnore = Arrays.asList("uid.dat", "session.dat");

    public static void copyWorld(File source, File target){
        try {
            if(!toIgnore.contains(source.getName())) {
                if(source.isDirectory()) {
                    if(!target.exists())
                        target.mkdirs();
                    String files[] = source.list();
                    for (String file : files) {
                        File srcFile = new File(source, file);
                        File destFile = new File(target, file);
                        copyWorld(srcFile, destFile);
                    }
                } else {
                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(target);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0)
                        out.write(buffer, 0, length);
                    in.close();
                    out.close();
                }
            }
        } catch (IOException ignored) {
        }
    }
}
