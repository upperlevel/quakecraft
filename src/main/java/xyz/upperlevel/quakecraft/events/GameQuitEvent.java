package xyz.upperlevel.quakecraft.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.quakecraft.game.Game;

@Getter
public class GameQuitEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Game game;
    private final Player player;

    public GameQuitEvent(Game game, Player player) {
        this.game = game;
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
