package xyz.upperlevel.quakecraft.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.quakecraft.QuakeAccount;

@RequiredArgsConstructor
public class PlayerDashCooldownEnd extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final QuakeAccount player;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}