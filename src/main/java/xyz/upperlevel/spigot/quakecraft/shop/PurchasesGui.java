package xyz.upperlevel.spigot.quakecraft.shop;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.QuakePlayerManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;
import xyz.upperlevel.uppercore.gui.ChestGui;
import xyz.upperlevel.uppercore.gui.config.economy.Balance;
import xyz.upperlevel.uppercore.gui.config.economy.EconomyManager;
import xyz.upperlevel.uppercore.placeholder.PlaceholderUtil;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.util.*;
import java.util.stream.Collectors;

public class PurchasesGui<P extends Purchase> extends ChestGui {
    private static List<PlaceholderValue<String>> buyingLores, boughtLores;
    private final int usableSlots[];
    private final PurchaseManager<P> purchaseManager;
    private Map<Integer, P> purchaseMap = new HashMap<>();

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
        QuakePlayer qp = QuakePlayerManager.get().getPlayer(player);
        for(Map.Entry<Integer, P> p  : purchaseMap.entrySet())
            inv.setItem(p.getKey(), getIcon(p.getValue(), qp));
        return inv;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        P p = purchaseMap.get(event.getSlot());
        if(p != null)
            onClick((Player) event.getWhoClicked(), event.getSlot(), p);
        else
            super.onClick(event);
    }

    public void onClick(Player player, int slot, P purchase) {
        QuakePlayer p = QuakePlayerManager.get().getPlayer(player);
        Set<Purchase<?>> purchases = p.getPurchases();
        if(!purchases.contains(purchase)) {
            Balance b = EconomyManager.get(player);
            if(b.take(purchase.getCost())) {
                purchases.add(purchase);
                player.getOpenInventory().getTopInventory().setItem(slot, getIcon(purchase, p));//Reload the item
            }
        }
    }

    protected ItemStack getIcon(P purchase, QuakePlayer p) {//Maybe we need to use the QuakePlayer :/
        ItemStack i = purchase.getIcon().toItemStack(p.getPlayer());
        ItemMeta meta = i.getItemMeta();
        final Player player = p.getPlayer();
        if(p.getPurchases().contains(purchase))
            meta.getLore().addAll(boughtLores.stream().map(v -> v.resolve(player)).collect(Collectors.toList()));
        else
            meta.getLore().addAll(buyingLores.stream().map(v -> v.resolve(player)).collect(Collectors.toList()));
        i.setItemMeta(meta);
        return i;
    }

    public static <P extends Purchase> PurchasesGui<P> deserialize(Plugin plugin, String id, Config config, PurchaseManager<P> purchaseManager) {
        try {
            return new PurchasesGui<>(plugin, id, config, purchaseManager);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in gui " + id);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public static void load(Config config) {//TODO add %cost% local placehodler
        buyingLores = ((Collection<Object>)config.getCollectionRequired("buying")).stream().map(o -> PlaceholderUtil.process(o.toString())).collect(Collectors.toList());
        boughtLores = ((Collection<Object>)config.getCollectionRequired("bought")).stream().map(o -> PlaceholderUtil.process(o.toString())).collect(Collectors.toList());
    }
}
