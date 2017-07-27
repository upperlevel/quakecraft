package xyz.upperlevel.spigot.quakecraft.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;

@RequiredArgsConstructor
public class PlayerDashCooldownEnd extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final QuakePlayer player;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}