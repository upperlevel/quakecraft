package xyz.upperlevel.spigot.quakecraft.powerup.effects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.game.Participant;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

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
    public void apply(Participant player) {
        start(player);
        Collection<Player> messageReceivers = player.getPhase().getGame().getPlayers();
        activeMessage.broadcast(messageReceivers, "name", player.getName());
        final BukkitTask task = Bukkit.getScheduler().runTaskLater(
                QuakeCraftReloaded.get(),
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

    public void load(Config config) {
        super.load(config);
        this.duration = config.getIntRequired("duration");
        MessageManager messages = MessageManager.load(config.getConfigRequired("messages"));
        activeMessage = messages.get("active");
        deactiveMessage = messages.get("deactive");
    }
}
