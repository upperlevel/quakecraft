package xyz.upperlevel.quakecraft.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.quakecraft.profile.Profile;

@Getter
public class PlayerDashEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Profile player;
    @Setter
    private float power;
    @Setter
    private int cooldown;

    @Setter
    private boolean cancelled;

    public PlayerDashEvent(Profile player, float power, int cooldown) {
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

