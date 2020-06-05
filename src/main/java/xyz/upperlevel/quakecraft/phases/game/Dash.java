package xyz.upperlevel.quakecraft.phases.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.quakecraft.events.PlayerDashCooldownEnd;
import xyz.upperlevel.quakecraft.events.PlayerDashEvent;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.placeholder.message.Message;

import java.util.HashMap;
import java.util.Map;

import static xyz.upperlevel.quakecraft.phases.game.GamePhase.gunSlot;

public class Dash {
    public static final int MILLIS_IN_TICK = 50;
    private static float BASE_POWER = 2f;

    private static Message COOLDOWN_MESSAGE;

    private static final Map<Player, Dash> dashing = new HashMap<>();

    private final QuakeAccount player;

    private long startTime;
    private long endTime;

    public Dash(QuakeAccount player) {
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
        int cooldownTicks = event.getCooldown();

        BukkitScheduler scheduler = Bukkit.getScheduler();

        Player p = player.getPlayer();
        dashing.put(p, this);
        p.setCooldown(p.getInventory().getItem(gunSlot).getType(), cooldownTicks);
        scheduler.runTaskLater(Quake.get(), this::cooldownEnd, cooldownTicks);
        p.setVelocity(p.getLocation().getDirection().multiply(power * BASE_POWER));

        startTime = System.currentTimeMillis();
        endTime = startTime + (cooldownTicks * MILLIS_IN_TICK);
    }

    public void cooldownEnd() {
        Bukkit.getPluginManager().callEvent(new PlayerDashCooldownEnd(player));
        dashing.remove(player.getPlayer());
    }

    public static void swish(Player p) {
        Dash dash = dashing.get(p);
        if (dash != null) {
            COOLDOWN_MESSAGE.send(p, "remaining_secs", String.valueOf((int)Math.ceil((dash.endTime - System.currentTimeMillis())/1000f)));
            return;
        }

        QuakeAccount player = Quake.get().getPlayerManager().getAccount(p);
        new Dash(player).swish();
    }

    public static void loadConfig() {
        Config conf = Quake.getConfigSection("game");
        BASE_POWER = conf.getFloatRequired("dash-power");
        COOLDOWN_MESSAGE = conf.getMessage("dash-cooldown-message");
    }
}
