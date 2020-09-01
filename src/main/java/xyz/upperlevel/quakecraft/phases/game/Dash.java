package xyz.upperlevel.quakecraft.phases.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.events.PlayerDashCooldownEnd;
import xyz.upperlevel.quakecraft.events.PlayerDashEvent;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.placeholder.message.Message;

import java.util.HashMap;
import java.util.Map;

import static xyz.upperlevel.quakecraft.phases.game.GamePhase.hotbar;

public class Dash {
    public static final int MILLIS_IN_TICK = 50;

    private static Message COOLDOWN_MESSAGE;

    private static final Map<Player, Dash> dashing = new HashMap<>();

    private final Profile profile;

    private long startTime;
    private long endTime;

    public Dash(Profile profile) {
        this.profile = profile;
    }

    public void swish() {
        PlayerDashEvent event = new PlayerDashEvent(profile, profile.getSelectedDashPower().getPower(), profile.getSelectedDashCooldown().getCooldown());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            dashing.remove(profile.getPlayer());
            return;
        }

        float power = event.getPower();
        int cooldownTicks = event.getCooldown();

        BukkitScheduler scheduler = Bukkit.getScheduler();

        Player p = profile.getPlayer();
        dashing.put(p, this);
        p.setCooldown(p.getInventory().getItem(hotbar.getGunSlot()).getType(), cooldownTicks);
        scheduler.runTaskLater(Quake.get(), this::cooldownEnd, cooldownTicks);

        Gamer gamer = Quake.getGamer(profile.getPlayer());
        float basePower = power * 2;
        p.setVelocity(p.getLocation().getDirection().multiply(basePower * gamer.getDashBoostBase()));

        startTime = System.currentTimeMillis();
        endTime = startTime + (cooldownTicks * MILLIS_IN_TICK);
    }

    public void cooldownEnd() {
        Bukkit.getPluginManager().callEvent(new PlayerDashCooldownEnd(profile));
        dashing.remove(profile.getPlayer());
    }

    public static void swish(Player p) {
        Dash dash = dashing.get(p);
        if (dash != null) {
            COOLDOWN_MESSAGE.send(p, "remaining_secs", String.valueOf((int)Math.ceil((dash.endTime - System.currentTimeMillis())/1000f)));
            return;
        }

        Profile profile = Quake.getProfileController().getProfileCached(p);
        new Dash(profile).swish();
    }

    public static void loadConfig() {
        Config conf = Quake.getConfigSection("game");
        COOLDOWN_MESSAGE = conf.getMessage("dash-cooldown-message");
    }
}
