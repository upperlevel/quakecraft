package xyz.upperlevel.quakecraft.shop.require.impl;

import xyz.upperlevel.quakecraft.QuakePlayer;
import xyz.upperlevel.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.quakecraft.shop.require.Require;

public class RequirePermission implements Require {
    private String permission;

    public RequirePermission(Purchase<?> parent, Object par) {
        this.permission = String.valueOf(par);
    }

    @Override
    public String name(QuakePlayer player) {
        return permission;
    }

    @Override
    public String type() {
        return "permission";
    }

    @Override
    public String description() {
        return null;
    }

    @Override
    public boolean test(QuakePlayer player) {
        return player.getPlayer().hasPermission(permission);
    }

    @Override
    public String getProgress() {
        return null;
    }
}
