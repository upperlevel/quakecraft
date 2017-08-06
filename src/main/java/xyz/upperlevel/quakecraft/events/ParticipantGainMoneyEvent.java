package xyz.upperlevel.spigot.quakecraft.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.spigot.quakecraft.game.Participant;
import xyz.upperlevel.spigot.quakecraft.game.gains.GainType;

@Getter
@Setter
public class ParticipantGainMoneyEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Participant player;
    private final GainType type;
    private float gain;

    private boolean cancelled;


    public ParticipantGainMoneyEvent(Participant player, GainType type) {
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
