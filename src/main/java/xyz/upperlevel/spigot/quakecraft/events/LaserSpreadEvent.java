package xyz.upperlevel.spigot.quakecraft.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.spigot.quakecraft.game.PlayingPhase;

/**
 * Event called when the laser is travelling along its trajectory.
 */
@Getter
@Setter
public class LaserSpreadEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Game game;
    private final PlayingPhase phase;
    private final Location location;
    private final Player shooter;

    public LaserSpreadEvent(PlayingPhase phase, Location location, Player shooter) {
        this.phase = phase;
        this.game = phase.getGame();
        this.location = location;
        this.shooter = shooter;
    }

    private boolean cancelled;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
