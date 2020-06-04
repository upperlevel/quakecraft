package xyz.upperlevel.quakecraft.phases.game;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.upperlevel.quakecraft.Quake;

import java.util.Random;

public class WinnerCelebration extends BukkitRunnable {
    public static final int fireworkFrequency = 1; // Fireworks per second to spawn.

    private final Random random = new Random();
    private final Player winner;

    public WinnerCelebration(Player winner) {
        this.winner = winner;
    }

    public void start() {
        runTaskTimer(Quake.get(), 0, fireworkFrequency * 20);
    }

    private Color getFireworkColor() {
        return Color.fromRGB(
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255)
        );
    }

    @Override
    public void run() {
        Firework firework = winner.getPlayer().getWorld().spawn(winner.getPlayer().getLocation(), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(
                FireworkEffect.builder()
                        .withColor(getFireworkColor())
                        .build()
        );
        meta.setPower(1);
        firework.setFireworkMeta(meta);
    }
}
