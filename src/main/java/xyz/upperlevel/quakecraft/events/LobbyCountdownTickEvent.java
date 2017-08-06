package xyz.upperlevel.quakecraft.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.quakecraft.game.Game;
import xyz.upperlevel.quakecraft.game.LobbyPhase;

@Getter
@RequiredArgsConstructor
public class LobbyCountdownTickEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Game game;
    private final LobbyPhase phase;
    private final int timer;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
