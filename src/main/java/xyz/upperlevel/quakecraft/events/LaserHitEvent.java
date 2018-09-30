package xyz.upperlevel.quakecraft.events;


import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.quakecraft.phases.Gamer;

public class LaserHitEvent extends Event implements Cancellable {
    @Getter
    private final QuakeArena arena;

    @Getter
    private final Gamer shooter;

    @Getter
    private final Gamer hit;

    @Getter
    private final boolean headshot;

    @Getter
    @Setter
    private boolean cancelled;

    public LaserHitEvent(QuakeArena arena,
                         Gamer shooter,
                         Gamer hit,
                         boolean headshot) {
        this.arena = arena;
        this.shooter = shooter;
        this.hit = hit;
        this.headshot = headshot;
    }

    @Override
    public HandlerList getHandlers() {
        return null;
    }
}
