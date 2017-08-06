package xyz.upperlevel.spigot.quakecraft.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.spigot.quakecraft.game.LobbyPhase;

@Getter
@Setter
@RequiredArgsConstructor
public class LobbyCountdownEndEvent extends Event implements Cancellable {

    public enum Reason {
        FEW_PLAYERS,
        END_TIMER,
        INTERRUPT_PHASE
    }

    private static final HandlerList handlers = new HandlerList();

    private final Game game;
    private final LobbyPhase phase;
    private final Reason reason;

    private boolean cancelled;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
