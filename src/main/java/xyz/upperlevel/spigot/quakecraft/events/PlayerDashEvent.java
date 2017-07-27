package xyz.upperlevel.spigot.quakecraft.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;

@Getter
public class PlayerDashEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final QuakePlayer player;
    @Setter
    private float power;
    @Setter
    private float cooldown;

    @Setter
    private boolean cancelled;

    public PlayerDashEvent(QuakePlayer player, float power, float cooldown) {
        this.player = player;
        this.power = power;
        this.cooldown = cooldown;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

