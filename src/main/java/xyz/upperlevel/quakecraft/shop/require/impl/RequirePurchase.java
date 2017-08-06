package xyz.upperlevel.spigot.quakecraft.shop.require.impl;

import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.PurchaseManager;
import xyz.upperlevel.spigot.quakecraft.shop.require.Require;

public class RequirePurchase implements Require {
    private final Purchase parent;
    private String requiredId;
    private Purchase<?> required;//Lazy initialization to avoid initialization errors

    public RequirePurchase(Purchase<?> parent, Object par) {
        this.parent = parent;
        this.requiredId = String.valueOf(par);
    }

    @Override
    public String name(QuakePlayer player) {
        return getRequired().getName().resolve(player.getPlayer());
    }

    @Override
    public String type() {
        return "purchase";
    }

    @Override
    public String description() {
        return null;
    }

    @Override
    public boolean test(QuakePlayer player) {
        return player.getPurchases().contains(getRequired());
    }

    @Override
    public String getProgress() {
        return null;
    }

    public Purchase<?> getRequired() {
        if(required == null) {
            PurchaseManager manager = parent.getManager();
            if (requiredId.indexOf(':') < 0) {
                required = manager.get(requiredId);
                if(required == null)
                    throw new IllegalArgumentException("Cannot find purchase '" + requiredId + "'");
            } else
                required = manager.getRegistry().getPurchase(requiredId);
        }
        return required;
    }
}
