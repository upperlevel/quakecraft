package xyz.upperlevel.quakecraft.powerup.effects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.phases.Gamer;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.placeholder.message.Message;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class TimeBasedPowerupEffect extends BasePowerupEffect {
    @Getter
    @Setter
    private int duration;
    private Message activeMessage;
    private Message deactiveMessage;
    private Map<Player, BukkitTask> tasks = new HashMap<>();

    public TimeBasedPowerupEffect(String id) {
        super(id);
    }

    @Override
    public void apply(Gamer player) {
        start(player);
        Collection<Player> messageReceivers = player.getArena().getPlayers();
        activeMessage.broadcast(messageReceivers, "name", player.getName());
        final BukkitTask task = Bukkit.getScheduler().runTaskLater(
                Quake.get(),
                () -> {
                    tasks.remove(player.getPlayer());
                    end(player);
                    deactiveMessage.broadcast(messageReceivers, "name", player.getName());
                },
                duration
        );
        BukkitTask old = tasks.put(player.getPlayer(), task);
        if(old != null)
            old.cancel();
    }

    public abstract void start(Gamer player);

    public abstract void end(Gamer player);

    @Override
    public void clear(Collection<Gamer> players) {
        for(Gamer p : players) {
            BukkitTask task = tasks.remove(p.getPlayer());
            if(task != null) {
                task.cancel();
                end(p);
            }
        }
    }

    public void load(Config config) {
        super.load(config);
        this.duration = config.getIntRequired("duration");

        Config messages = config.getConfigRequired("messages");
        activeMessage = messages.getMessageRequired("active");
        deactiveMessage = messages.getMessageRequired("deactive");
    }
}
