package xyz.upperlevel.spigot.quakecraft.shop.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.Purchase;

@Getter
@RequiredArgsConstructor
public class PurchaseBuyEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final QuakePlayer player;
    private final Purchase<?> purchase;

    @Setter
    private boolean cancelled = false;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
