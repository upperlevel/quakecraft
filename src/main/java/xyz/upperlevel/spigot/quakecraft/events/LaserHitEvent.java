package xyz.upperlevel.spigot.quakecraft.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.spigot.quakecraft.game.Game;

@Getter
@RequiredArgsConstructor
public class LaserHitEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Game game;
    private final Location location;
    private final Player shooter, hit;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
