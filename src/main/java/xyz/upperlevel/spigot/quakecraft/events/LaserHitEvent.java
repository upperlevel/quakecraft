package xyz.upperlevel.spigot.quakecraft.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.game.play.PlayingPhase;

@Getter
@RequiredArgsConstructor
public class LaserHitEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final PlayingPhase phase;
    private final Location location;
    private final QuakePlayer qShooter;
    private final Player shooter, hit;

    @Setter
    private boolean cancelled;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
