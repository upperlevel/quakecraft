package xyz.upperlevel.spigot.quakecraft.powerup.effects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.game.Participant;
import xyz.upperlevel.uppercore.config.Config;

import java.util.HashMap;
import java.util.Map;

public abstract class TimeBasedPowerupEffect extends BasePowerupEffect {
    @Getter
    @Setter
    private int duration;
    private Map<Player, BukkitTask> tasks = new HashMap<>();

    public TimeBasedPowerupEffect(String id) {
        super(id);
    }

    @Override
    public void apply(Participant participant) {
        start(participant);
        final BukkitTask task = Bukkit.getScheduler().runTaskLater(
                QuakeCraftReloaded.get(),
                () -> {
                    tasks.remove(participant.getPlayer());
                    end(participant);
                },
                duration
        );
        BukkitTask old = tasks.put(participant.getPlayer(), task);
        if(old != null)
            old.cancel();
    }

    public abstract void start(Participant player);


    public abstract void end(Participant player);

    public void load(Config config) {
        super.load(config);
        this.duration = config.getIntRequired("duration");
    }
}
