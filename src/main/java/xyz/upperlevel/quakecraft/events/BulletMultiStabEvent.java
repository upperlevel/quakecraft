package xyz.upperlevel.quakecraft.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.quakecraft.game.GamePhase;
import xyz.upperlevel.quakecraft.game.playing.Bullet;
import xyz.upperlevel.quakecraft.game.playing.MultiStab;
import xyz.upperlevel.uppercore.message.Message;

@Getter
@Setter
public class BulletMultiStabEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final GamePhase phase;
    private final Bullet bullet;
    private final MultiStab stab;
    private Message message;

    private boolean cancelled;


    public BulletMultiStabEvent(GamePhase phase, Bullet bullet, MultiStab stab, Message message) {
        this.phase = phase;
        this.bullet = bullet;
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