package xyz.upperlevel.quakecraft.powerup.effects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitTask;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.events.GameQuitEvent;
import xyz.upperlevel.quakecraft.game.GamePhase;
import xyz.upperlevel.quakecraft.game.Participant;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;
import xyz.upperlevel.uppercore.util.nms.impl.TagNms;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
    public void apply(Participant player) {
        start(player);
        Collection<Player> messageReceivers = player.getPhase().getGame().getPlayers();
        activeMessage.broadcast(messageReceivers, "name", player.getName());
        final BukkitTask task = Bukkit.getScheduler().runTaskLater(
                Quakecraft.get(),
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

    public abstract void start(Participant player);

    public abstract void end(Participant player);

    @Override
    public void clear(Collection<Participant> players) {
        for(Participant p : players) {
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
        MessageManager messages = MessageManager.load(config.getConfigRequired("messages"));
        activeMessage = messages.get("active");
        deactiveMessage = messages.get("deactive");
    }
}
