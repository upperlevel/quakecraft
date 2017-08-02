package xyz.upperlevel.spigot.quakecraft.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.spigot.quakecraft.game.GamePhase;
import xyz.upperlevel.spigot.quakecraft.game.play.Bullet;
import xyz.upperlevel.spigot.quakecraft.game.play.MultiStab;
import xyz.upperlevel.uppercore.message.Message;

@Getter
@Setter
public class MultiStabEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final GamePhase phase;
    private final Bullet bullet;
    private final MultiStab stab;
    private Message message;

    private boolean cancelled;


    public MultiStabEvent(GamePhase phase, Bullet bullet, MultiStab stab, Message message) {
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
