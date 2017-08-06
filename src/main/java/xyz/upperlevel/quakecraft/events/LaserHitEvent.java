package xyz.upperlevel.spigot.quakecraft.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.game.play.PlayingPhase;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class LaserHitEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final PlayingPhase phase;
    private final QuakePlayer shooter;
    private final List<Player> victims;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
