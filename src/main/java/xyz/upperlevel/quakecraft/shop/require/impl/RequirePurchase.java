package xyz.upperlevel.quakecraft.shop.require.impl;

import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseManager;
import xyz.upperlevel.quakecraft.shop.require.Require;

public class RequirePurchase implements Require {
    private final Purchase parent;
    private String requiredId;
    private Purchase<?> required;//Lazy initialization to avoid initialization errors

    public RequirePurchase(Purchase<?> parent, String par) {
        this.parent = parent;
        this.requiredId = par;
    }

    @Override
    public String getName(QuakeAccount player) {
        return getRequired().getName().resolve(player.getPlayer());
    }

    @Override
    public String getType() {
        return "purchase";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public boolean test(QuakeAccount player) {
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
