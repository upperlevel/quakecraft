package xyz.upperlevel.spigot.quakecraft.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.spigot.quakecraft.game.GamePhase;
import xyz.upperlevel.spigot.quakecraft.game.Participant;
import xyz.upperlevel.spigot.quakecraft.game.play.KillStreak;
import xyz.upperlevel.uppercore.message.Message;

@Getter
@Setter
public class KillStreakReachEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final GamePhase phase;
    private final Participant player;
    private final KillStreak streak;
    private Message message;

    private boolean cancelled;

    public KillStreakReachEvent(GamePhase phase, Participant player, KillStreak streak, Message message) {
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
