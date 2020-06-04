package xyz.upperlevel.quakecraft.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.quakecraft.phases.game.GamePhase;
import xyz.upperlevel.quakecraft.phases.game.Gamer;
import xyz.upperlevel.quakecraft.phases.game.KillStreak;
import xyz.upperlevel.uppercore.placeholder.message.Message;

@Getter
@Setter
public class KillStreakReachEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final GamePhase phase;
    private final Gamer player;
    private final KillStreak streak;
    private Message message;

    private boolean cancelled;

    public KillStreakReachEvent(GamePhase phase, Gamer player, KillStreak streak, Message message) {
        this.phase = phase;
        this.player = player;
        this.streak = streak;
        this.message = message;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
