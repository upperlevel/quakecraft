package xyz.upperlevel.spigot.quakecraft.shop.purchase;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Sound;
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
import xyz.upperlevel.spigot.quakecraft.core.EnchantGlow;
import xyz.upperlevel.spigot.quakecraft.core.PlayerUtil;
import xyz.upperlevel.spigot.quakecraft.shop.require.Require;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;
import xyz.upperlevel.uppercore.economy.Balance;
import xyz.upperlevel.uppercore.economy.EconomyManager;
import xyz.upperlevel.uppercore.gui.ChestGui;
import xyz.upperlevel.uppercore.itemstack.CustomItem;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.util.*;
import java.util.stream.Collectors;

public class PurchaseGui extends ChestGui {
    private static List<PlaceholderValue<String>> buyingLores, boughtLores, selectedLores;
    @Getter
    private List<PurchaseAdapter> adapters = new ArrayList<>();
    private Map<Integer, SimplePurchase<?>> purchaseMap = new LinkedHashMap<>();
    @Getter
    private boolean dirty = true;
    @Getter
    @Setter
    private boolean enchantSelected = true;

    public PurchaseGui(int size, String title) {
        super(size, title);
    }

    public PurchaseGui(InventoryType type, String title) {
        super(type, title);
    }

    public PurchaseGui(Plugin plugin, Config config) {
        super(plugin, config);
    }

    public void update() {
        this.dirty = true;
    }

    public void reprint() {
        this.purchaseMap.clear();

        for(PurchaseAdapter adapter : adapters) {
            Collection<SimplePurchase<?>> purchases = (Collection<SimplePurchase<?>>) adapter.manager.getPurchases().values();
            int[] slots = adapter.slots;
            int i = 0;
            for (SimplePurchase<?> p : purchases) {
                int slot = slots[i++];
                if (slot < 0) {
                    QuakeCraftReloaded.get().getLogger().severe("Cannot fill " + adapter.manager.getPurchaseName() + "'s inventory: too many items!");
                    return;
                }
                purchaseMap.put(slot, p);
            }
        }
        dirty = false;
    }

    @Override
    public Inventory create(Player player) {
        if (dirty)
            reprint();
        Inventory inv = super.create(player);
        QuakePlayer qp = QuakePlayerManager.get().getPlayer(player);
        if(qp == null) {
            QuakeCraftReloaded.get().getLogger().severe("Player not registered in quake registry: " + player.getName());
            return inv;
        }
        for (Map.Entry<Integer, SimplePurchase<?>> p : purchaseMap.entrySet())
            inv.setItem(p.getKey(), getIcon(p.getValue(), qp, p.getValue().isSelected(qp)));
        return inv;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        SimplePurchase<?> p = purchaseMap.get(event.getSlot());
        if (p != null)
            onClick((Player) event.getWhoClicked(), event.getSlot(), p);
        else
            super.onClick(event);
    }

    public void onClick(Player player, int slot, SimplePurchase<?> purchase) {
        QuakePlayer p = QuakePlayerManager.get().getPlayer(player);
        Set<Purchase<?>> purchases = p.getPurchases();
        if (!purchases.contains(purchase)) {
            //Require test
            if(purchase.getRequires().stream().anyMatch(r -> !r.test(p)))
                return;

            Balance b = EconomyManager.get(player);
            if (b == null) {
                QuakeCraftReloaded.get().getLogger().severe("Economy not found!");
                return;
            }
            if (b.take(purchase.getCost())) {
                purchases.add(purchase);
                p.getPurchases().add(purchase);
                reloadSelection(p, slot, purchase);
            } else
                PlayerUtil.playSound(player, Sound.BLOCK_ANVIL_BREAK);
        } else
            reloadSelection(p, slot, purchase);
    }

    protected ItemStack getIcon(Purchase<?> purchase, QuakePlayer player, boolean selected) {
        CustomItem icon = purchase.getIcon(player);
        Player p = player.getPlayer();
        if (icon == null) {
            QuakeCraftReloaded.get().getLogger().severe("Null icon for purchase: \"" + purchase.getName());
            return null;
        }
        ItemStack item = icon.resolve(p);
        ItemMeta meta = item.getItemMeta();

        processMeta(player, purchase, meta, selected);

        item.setItemMeta(meta);
        return item;
    }

    public void processMeta(QuakePlayer player, Purchase<?> purchase, ItemMeta meta, boolean selected) {
        Player p = player.getPlayer();
        meta.setDisplayName(purchase.getName().resolve(p));

        if(selected && enchantSelected)
            addGlow(meta);

        //Lore processing (additional lore + requires)
        List<String> metaLore = meta.getLore();
        if (metaLore == null)
            metaLore = new ArrayList<>();

        metaLore.addAll(
                getLore(player, purchase, selected)
                        .stream()
                        .map(lore -> lore.resolve(p, purchase.getPlaceholders()))
                        .collect(Collectors.toList())
        );

        // Add requires
        metaLore.addAll(processRequires(purchase, player));

        meta.setLore(metaLore);
    }

    public List<String> processRequires(Purchase<?> purchase, QuakePlayer player) {
        List<String> lore = new ArrayList<>();
        List<Require> requires = purchase.getRequires();
        for(Require require : requires) {
            String req = require.getRequires(player);
            boolean pass = require.test(player);
            String pre = pass ? Require.DONE : Require.MISSING;
            String description = require.description();
            if(description != null)//If there's no description nor progress the require is displayed as a one-line require
                lore.add("");
            lore.add(" " + pre + " " + req);

            if(description != null) {
                lore.add("   " + description);
                if (!pass) {
                    String progress = require.getProgress();
                    if (progress != null)
                        lore.add("   " + progress);
                }
            }
        }
        return lore;
    }

    protected void addGlow(ItemMeta meta) {
        EnchantGlow.addGlow(meta);
    }

    protected List<PlaceholderValue<String>> getLore(QuakePlayer player, Purchase purchase, boolean selected) {
        if (selected)
            return selectedLores;
        else if (player.getPurchases().contains(purchase))
            return boughtLores;
        else
            return buyingLores;
    }

    public Map<Integer, SimplePurchase<?>> getPurchaseMap() {
        return Collections.unmodifiableMap(purchaseMap);
    }

    @SuppressWarnings("unchecked")
    protected void reloadSelection(QuakePlayer player, int slot, SimplePurchase<?> sel) {
        PurchaseManager manager = sel.getManager();
        Purchase oldPurchase = manager.getSelected(player);
        manager.setSelected(player, sel);
        if (oldPurchase == sel)
            return;
        Inventory inv = player.getPlayer().getOpenInventory().getTopInventory();
        int oldSelIndex = getPurchaseMap()
                .entrySet()
                .stream()
                .filter(e -> e.getValue() == oldPurchase)
                .findAny().orElseThrow(() -> new IllegalStateException("No old selection?"))
                .getKey();

        inv.setItem(oldSelIndex, getIcon(oldPurchase, player, false));
        inv.setItem(slot, getIcon(sel, player, true));
    }

    public void add(PurchaseManager<?> manager, int[] slots) {
        adapters.add(new PurchaseAdapter(manager, slots));
    }

    @SuppressWarnings("unchecked")
    public static void load(Config config) {
        buyingLores = config.getMessageListRequired("buying");
        boughtLores = config.getMessageListRequired("bought");
        selectedLores = config.getMessageListRequired("selected");
        QuakeCraftReloaded.get().getLogger().info("PurchaseGui's config loaded!");
    }

    @SuppressWarnings("unchecked")
    public static PurchaseGui deserialize(Plugin plugin, String id, Config config, PurchaseManager manager) {
        try {
            PurchaseGui gui = new PurchaseGui(plugin, config);
            gui.add(
                    manager,
                    deserialize(config.getCollectionRequired("slots"))
            );
            return gui;
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in gui " + id);
            throw e;
        }
    }

    public static int[] deserialize(Collection<?> slots) {
        return slots.stream().mapToInt(o -> {
            if (o instanceof Number)
                return ((Number) o).intValue();
            else
                throw new InvalidConfigurationException("Only numbers in the slots field!");
        }).toArray();
    }

    @AllArgsConstructor
    public static class PurchaseAdapter {
        private final PurchaseManager<?> manager;
        private final int[] slots;
    }
}
