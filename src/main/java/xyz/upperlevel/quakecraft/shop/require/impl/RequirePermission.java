package xyz.upperlevel.quakecraft.shop.require.impl;

import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.quakecraft.shop.require.Require;

public class RequirePermission implements Require {
    private String permission;

    public RequirePermission(Purchase<?> parent, String par) {
        this.permission = par;
    }

    @Override
    public String name(QuakeAccount player) {
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
    public boolean test(QuakeAccount player) {
        return player.getPlayer().hasPermission(permission);
    }

    @Override
    public String getProgress() {
        return null;
    }
}
