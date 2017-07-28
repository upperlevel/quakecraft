package xyz.upperlevel.spigot.quakecraft.game.play;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.events.PlayerDashCooldownEnd;
import xyz.upperlevel.spigot.quakecraft.events.PlayerDashEvent;

import java.util.HashSet;
import java.util.Set;

import static org.bukkit.ChatColor.RED;

public class Dash {
    public static final int MILLIS_IN_TICK = 50;

    private static final float defDashPower = 2f;//TODO: config
    private static final Set<Player> dashing = new HashSet<>();

    private final QuakePlayer player;

    public Dash(QuakePlayer player) {
        this.player = player;
    }

    public void swish() {
        PlayerDashEvent event = new PlayerDashEvent(player, player.getSelectedDashPower().getPower(), player.getSelectedDashCooldown().getCooldown());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            dashing.remove(player.getPlayer());
            return;
        }

        float power = event.getPower();
        long cooldownMillis = (long) (event.getCooldown() * 1000);

        BukkitScheduler scheduler = Bukkit.getScheduler();

        scheduler.runTaskLater(QuakeCraftReloaded.get(), this::cooldownEnd, cooldownMillis/MILLIS_IN_TICK);
        player.getPlayer().setVelocity(player.getPlayer().getLocation().getDirection().multiply(power * defDashPower));
    }

    public void cooldownEnd() {
        Bukkit.getPluginManager().callEvent(new PlayerDashCooldownEnd(player));
        dashing.remove(player.getPlayer());
    }

    public static void dash(Player p) {
        if (dashing.add(p)) {
            p.sendMessage(RED + "Dash cooling down");
            return;
        }

        QuakePlayer player = QuakeCraftReloaded.get().getPlayerManager().getPlayer(p);
        new Dash(player).swish();
    }
}
