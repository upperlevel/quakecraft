package xyz.upperlevel.spigot.quakecraft.shop;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;
import xyz.upperlevel.uppercore.gui.ChestGui;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PurchasesGui<P extends Purchase> extends ChestGui {
    private final int usableSlots[];
    private final PurchaseManager<P> purchaseManager;
    private Map<Integer, Purchase> purchaseMap = new HashMap<>();

    public PurchasesGui(Plugin plugin, String id, int size, String title, PurchaseManager<P> manager, int[] usableSlots) {
        super(plugin, id, size, title);
        this.purchaseManager = manager;
        this.usableSlots = usableSlots;
    }

    public PurchasesGui(Plugin plugin, String id, InventoryType type, String title, PurchaseManager<P> manager, int[] usableSlots) {
        super(plugin, id, type, title);
        this.purchaseManager = manager;
        this.usableSlots = usableSlots;
    }

    @SuppressWarnings("unchecked")
    protected PurchasesGui(Plugin plugin, String id, Config config, PurchaseManager<P> purchaseManager) {
        super(plugin, id, config);
        this.purchaseManager = purchaseManager;
        this.usableSlots = config.getCollectionRequired("slots").stream().mapToInt(o -> {
            if(o instanceof Number)
                return ((Number) o).intValue();
            else
                throw new InvalidConfigurationException("Only numbers in the slots field!");
        }).toArray();
    }

    public void update() {
        this.purchaseMap.clear();
        int i = 0;
        Collection<P> purchases = purchaseManager.getPurchases().values();
        for(P p : purchases) {
            if(i >= usableSlots.length) {
                QuakeCraftReloaded.get().getLogger().severe("Cannot fill " + purchaseManager.getPurchaseName() + "'s inventory: too many items!");
                return;
            }
            int slot = usableSlots[i++];
            purchaseMap.put(slot, p);
        }
    }

    @Override
    public Inventory create(Player player) {
        Inventory inv = super.create(player);
        for(Map.Entry<Integer, Purchase> p : purchaseMap.entrySet())
            inv.setItem(p.getKey(), p.getValue().getIcon().toItemStack(player));
        return inv;
    }

    public static <P extends Purchase> PurchasesGui<P> deserialize(Plugin plugin, String id, Config config, PurchaseManager<P> purchaseManager) {
        return new PurchasesGui<>(plugin, id, config, purchaseManager);
    }
}
