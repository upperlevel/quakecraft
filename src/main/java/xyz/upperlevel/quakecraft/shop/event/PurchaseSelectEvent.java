package xyz.upperlevel.quakecraft.shop.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.quakecraft.shop.purchase.Purchase;

@Getter
public class PurchaseSelectEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final QuakeAccount player;

    private final Purchase<?> oldPurchase;
    @Setter
    private Purchase<?> purchase;
    @Setter
    private boolean cancelled = false;

    public PurchaseSelectEvent(QuakeAccount player, Purchase<?> oldPurchase, Purchase<?> purchase) {
        this.player = player;
        this.oldPurchase = oldPurchase;
        this.purchase = purchase;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
