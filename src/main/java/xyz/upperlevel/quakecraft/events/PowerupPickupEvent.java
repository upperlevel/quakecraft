package xyz.upperlevel.quakecraft.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.quakecraft.phases.Gamer;
import xyz.upperlevel.quakecraft.powerup.Powerup;

public class PowerupPickupEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final Powerup powerup;

    @Getter
    private final Gamer player;

    @Getter
    @Setter
    private boolean cancelled;

    public PowerupPickupEvent(Powerup powerup, Gamer player) {
        this.powerup = powerup;
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
