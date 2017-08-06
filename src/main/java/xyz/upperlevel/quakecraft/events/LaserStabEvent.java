package xyz.upperlevel.quakecraft.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.quakecraft.QuakePlayer;
import xyz.upperlevel.quakecraft.game.playing.PlayingPhase;

@Getter
@Setter
public class LaserStabEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final PlayingPhase phase;
    private final Location location;
    private final QuakePlayer qShooter;
    private final Player shooter, hit;
    private boolean headshot;

    private boolean cancelled;

    public LaserStabEvent(PlayingPhase phase, Location location, QuakePlayer qShooter, Player shooter, Player hit, boolean headshot) {
        this.phase = phase;
        this.location = location;
        this.qShooter = qShooter;
        this.shooter = shooter;
        this.hit = hit;
        this.headshot = headshot;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
