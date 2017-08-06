package xyz.upperlevel.spigot.quakecraft.game.play;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.events.PlayerDashCooldownEnd;
import xyz.upperlevel.spigot.quakecraft.events.PlayerDashEvent;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.message.Message;

import java.util.HashMap;
import java.util.Map;

import static org.bukkit.ChatColor.RED;

public class Dash {
    public static final int MILLIS_IN_TICK = 50;
    private static float BASE_POWER = 2f;
    private static Message COOLDOWN_MESSAGE;

    private static final Map<Player, Dash> dashing = new HashMap<>();

    private final QuakePlayer player;

    private long startTime;
    private long endTime;

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

        dashing.put(player.getPlayer(), this);
        scheduler.runTaskLater(QuakeCraftReloaded.get(), this::cooldownEnd, cooldownMillis/MILLIS_IN_TICK);
        player.getPlayer().setVelocity(player.getPlayer().getLocation().getDirection().multiply(power * BASE_POWER));

        startTime = System.currentTimeMillis();
        endTime = startTime + cooldownMillis;
    }

    public void cooldownEnd() {
        Bukkit.getPluginManager().callEvent(new PlayerDashCooldownEnd(player));
        dashing.remove(player.getPlayer());
    }

    public static void dash(Player p) {
        Dash dash = dashing.get(p);
        if (dash != null) {
            COOLDOWN_MESSAGE.send(p, "remaining_secs", String.valueOf((int)Math.ceil((dash.endTime - System.currentTimeMillis())/1000f)));
            return;
        }

        QuakePlayer player = QuakeCraftReloaded.get().getPlayerManager().getPlayer(p);
        new Dash(player).swish();
    }

    public static void loadConfig() {
        Config config = QuakeCraftReloaded.get().getCustomConfig().getConfigRequired("dash");
        BASE_POWER = config.getIntRequired("base-power");
        COOLDOWN_MESSAGE = QuakeCraftReloaded.get().getMessages().get("game.dash.cooldown");
    }
}
