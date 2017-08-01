package xyz.upperlevel.spigot.quakecraft.powerup.effects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.game.Participant;
import xyz.upperlevel.uppercore.config.Config;

public abstract class TimeBasedPowerupEffect extends BasePowerupEffect {
    @Getter
    @Setter
    private int duration;

    public TimeBasedPowerupEffect(String id) {
        super(id);
    }

    @Override
    public void apply(Participant participant) {
        start(participant);
        Bukkit.getScheduler().runTaskLater(
                QuakeCraftReloaded.get(),
                () -> end(participant),
                duration
        );
    }

    public abstract void start(Participant player);


    public abstract void end(Participant player);

    public void load(Config config) {
        super.load(config);
        this.duration = config.getIntRequired("duration");
    }
}
