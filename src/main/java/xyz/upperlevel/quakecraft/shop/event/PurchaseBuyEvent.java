package xyz.upperlevel.quakecraft.shop.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.quakecraft.shop.purchase.Purchase;

@Getter
@RequiredArgsConstructor
public class PurchaseBuyEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Profile profile;
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
