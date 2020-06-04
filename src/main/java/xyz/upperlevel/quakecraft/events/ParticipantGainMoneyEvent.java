package xyz.upperlevel.quakecraft.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.quakecraft.phases.game.Gamer;
import xyz.upperlevel.quakecraft.phases.game.GainType;

@Getter
@Setter
public class ParticipantGainMoneyEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Gamer player;
    private final GainType type;
    private float gain;

    private boolean cancelled;


    public ParticipantGainMoneyEvent(Gamer player, GainType type) {
        this.player = player;
        this.type = type;
        this.gain = type.getAmount();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
