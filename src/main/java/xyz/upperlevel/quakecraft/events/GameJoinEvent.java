package xyz.upperlevel.quakecraft.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.quakecraft.game.Game;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class GameJoinEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Game game;
    private final Player player;

    private List<String> kickReason = null;

    private boolean cancelled;

    public GameJoinEvent(Game game, Player player) {
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

    public void cancel(String... reason) {
        setCancelled(true);
        this.kickReason = Arrays.asList(reason);
    }

    public void cancel(List<String> reason) {
        setCancelled(true);
        this.kickReason = reason;
    }
}
