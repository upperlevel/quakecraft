package xyz.upperlevel.spigot.quakecraft.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.spigot.quakecraft.game.Participant;
import xyz.upperlevel.spigot.quakecraft.powerup.Powerup;

@RequiredArgsConstructor
@Data
public class ItemBoxPickupEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Powerup powerup;
    private final Participant player;

    private boolean cancelled;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
