package xyz.upperlevel.spigot.quakecraft.shop.purchase;

import xyz.upperlevel.spigot.quakecraft.QuakePlayer;

import java.util.HashMap;
import java.util.Map;

public class PurchaseRegistry {
    private Map<String, PurchaseManager> managersById = new HashMap<>();

    public void register(PurchaseManager manager) {
        if(managersById.putIfAbsent(manager.getPurchaseName(), manager) != null)
            throw new IllegalArgumentException("Tried to register multiple registers with the same name: '" + manager.getPurchaseName() + "'!");
    }

    public PurchaseManager getManager(String id) {
        return managersById.get(id);
    }

    public Purchase<?> getPurchase(String fullId) {
        String[] parts = fullId.split(":");
        if(parts.length != 2)
            throw new IllegalArgumentException("Cannot parse '" + fullId + "' as a Purchase full id!");
        PurchaseManager manager = getManager(parts[0]);
        if(manager == null)
            throw new IllegalArgumentException("Cannot find manager '" + parts[0] + "' in '" + fullId + "'!");
        Purchase<?> p = manager.get(parts[1]);
        if(p == null)
            throw new IllegalArgumentException("Cannot find purchase '" + parts[1] + "' in manager '" + parts[0] + "'!");
        return p;
    }

    public boolean hasPurchase(QuakePlayer player, String fullId) {
        return player.getPurchases().contains(getPurchase(fullId));
    }
}