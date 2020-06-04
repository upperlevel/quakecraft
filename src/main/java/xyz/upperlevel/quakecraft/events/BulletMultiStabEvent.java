package xyz.upperlevel.quakecraft.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.quakecraft.phases.game.GamePhase;
import xyz.upperlevel.quakecraft.phases.game.MultiStab;
import xyz.upperlevel.uppercore.placeholder.message.Message;

public class BulletMultiStabEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final GamePhase phase;

    @Getter
    private final MultiStab stab;

    @Getter
    @Setter
    private Message message;

    @Getter
    @Setter
    private boolean cancelled;


    public BulletMultiStabEvent(GamePhase phase, MultiStab stab, Message message) {
        this.phase = phase;
        this.stab = stab;
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
