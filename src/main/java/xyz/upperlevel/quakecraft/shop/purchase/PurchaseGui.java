package xyz.upperlevel.quakecraft.shop.purchase;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import xyz.upperlevel.quakecraft.QuakePlayer;
import xyz.upperlevel.quakecraft.QuakePlayerManager;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.shop.event.PurchaseBuyEvent;
import xyz.upperlevel.quakecraft.shop.event.PurchaseSelectEvent;
import xyz.upperlevel.quakecraft.shop.require.Require;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;
import xyz.upperlevel.uppercore.economy.Balance;
import xyz.upperlevel.uppercore.economy.EconomyManager;
import xyz.upperlevel.uppercore.gui.ChestGui;
import xyz.upperlevel.uppercore.gui.GuiAction;
import xyz.upperlevel.uppercore.gui.link.Link;
import xyz.upperlevel.uppercore.itemstack.CustomItem;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;
import xyz.upperlevel.uppercore.util.EnchantGlow;

import java.util.*;
import java.util.stream.Collectors;

public class PurchaseGui extends ChestGui {
    public static List<PlaceholderValue<String>> buyingLores, boughtLores, selectedLores;
    public static String prefixSelected, prefixSelectable, prefixBuying;
    public static Message notEnoughMoney;
    @Getter
    private List<PurchaseAdapter> adapters = new ArrayList<>();
    private Map<Integer, Purchase<?>> purchaseMap = new LinkedHashMap<>();
    @Getter
    private boolean dirty = true;
    @Getter
    @Setter
    private boolean enchantSelected = true;

    public PurchaseGui(int size, PlaceholderValue<String> title) {
        super(size, title);
    }

    public PurchaseGui(InventoryType type, PlaceholderValue<String> title) {
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
            if(purchases.size() > slots.length) {
                Quakecraft.get().getLogger().severe("Cannot fill " + adapter.manager.getPurchaseName() + "'s inventory: too many items!");
                return;
            }

            for (Purchase<?> p : purchases) {
                purchaseMap.put(slots[i++], p);
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
            Quakecraft.get().getLogger().severe("Player not registered in quake registry: " + player.getName());
            return inv;
        }
        printPurchases(inv, qp);
        return inv;
    }

    public void printPurchases(Inventory inv, QuakePlayer player) {
        for (Map.Entry<Integer, Purchase<?>> p : purchaseMap.entrySet())
            inv.setItem(p.getKey(), getIcon(p.getValue(), player, p.getValue().isSelected(player)));
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        Purchase<?> p = purchaseMap.get(event.getSlot());
        if (p != null)
            onClick((Player) event.getWhoClicked(), event.getSlot(), p);
        else
            super.onClick(event);
    }

    public void onClick(Player player, int slot, Purchase<?> purchase) {
        QuakePlayer p = QuakePlayerManager.get().getPlayer(player);
        Set<Purchase<?>> purchases = p.getPurchases();
        if (!purchases.contains(purchase)) {
            //Require test
            if(purchase.getRequires().stream().anyMatch(r -> !r.test(p)))
                return;

            if(purchase.getCost() > 0) {
                Balance b = EconomyManager.get(player);
                if (b == null) {
                    Quakecraft.get().getLogger().severe("Economy not found!");
                    return;
                }
                if (b.has(purchase.getCost())) {
                    Quakecraft.get().openConfirmPurchase(
                            player,
                            purchase,
                            ((Link) n -> onPurchaseSucceed(p, purchase)).and(GuiAction.back()),
                            GuiAction.back()
                    );
                } else {
                    notEnoughMoney.send(player, "required", String.valueOf(purchase.getCost()));
                }
            } else {
                onPurchaseSucceed(p, purchase);
                printPurchases(p.getPlayer().getOpenInventory().getTopInventory(), p);
            }
        } else
            reloadSelection(p, slot, purchase);
    }

    @SuppressWarnings("unchecked")
    public void onPurchaseSucceed(QuakePlayer player, Purchase purchase) {
        PluginManager eventManager = Bukkit.getPluginManager();

        PurchaseBuyEvent buyEvent = new PurchaseBuyEvent(player, purchase);
        eventManager.callEvent(buyEvent);
        if(!buyEvent.isCancelled()) {
            player.getPurchases().add(purchase);

            PurchaseManager purchaseManager = purchase.getManager();
            PurchaseSelectEvent event = new PurchaseSelectEvent(player, purchaseManager.getSelected(player), purchase);
            if (!event.isCancelled())
                purchaseManager.setSelected(player, event.getPurchase());
        }
    }

    protected ItemStack getIcon(Purchase<?> purchase, QuakePlayer player, boolean selected) {
        CustomItem icon = purchase.getIcon(player);
        Player p = player.getPlayer();
        if (icon == null) {
            Quakecraft.get().getLogger().severe("Null icon for purchase: \"" + purchase.getName());
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
        boolean purchased = selected || purchase.getCost() == 0 || player.getPurchases().contains(purchase);

        meta.setDisplayName(ChatColor.RESET + getPrefix(purchased, selected) + purchase.getName().resolve(p));

        if(selected && enchantSelected)
            addGlow(meta);

        //Lore processing (additional lore + requires)
        List<String> metaLore = meta.getLore();
        if (metaLore == null)
            metaLore = new ArrayList<>();

        metaLore.addAll(
                getLore(purchased, selected)
                        .stream()
                        .map(lore -> ChatColor.RESET + lore.resolve(p, purchase.getPlaceholders()))
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
            lore.add(ChatColor.RESET + " " + pre + " " + req);

            if(description != null) {
                lore.add(ChatColor.RESET + "   " + description);
                if (!pass) {
                    String progress = require.getProgress();
                    if (progress != null)
                        lore.add(ChatColor.RESET + "   " + progress);
                }
            }
        }
        return lore;
    }

    protected void addGlow(ItemMeta meta) {
        EnchantGlow.addGlow(meta);
    }

    protected List<PlaceholderValue<String>> getLore(boolean purchased, boolean selected) {
        if (selected)
            return selectedLores;
        else if (purchased)
            return boughtLores;
        else
            return buyingLores;
    }


    public static String getPrefix(boolean purchased, boolean selected) {
        if (selected)
            return prefixSelected;
        else if (purchased)
            return prefixSelectable;
        else
            return prefixBuying;
    }

    public Map<Integer, Purchase<?>> getPurchaseMap() {
        return Collections.unmodifiableMap(purchaseMap);
    }

    @SuppressWarnings("unchecked")
    protected void reloadSelection(QuakePlayer player, int slot, Purchase<?> sel) {
        PurchaseManager purchaseManager = sel.getManager();
        Purchase<?> old = purchaseManager.getSelected(player);
        if(old != sel) {
            PurchaseSelectEvent event = new PurchaseSelectEvent(player, old, sel);
            Bukkit.getPluginManager().callEvent(event);
            if(!event.isCancelled()) {
                purchaseManager.setSelected(player, event.getPurchase());
                printPurchases(player.getPlayer().getOpenInventory().getTopInventory(), player);
            }
        }
    }

    public void add(PurchaseManager<?> manager, int[] slots) {
        adapters.add(new PurchaseAdapter(manager, slots));
    }

    @SuppressWarnings("unchecked")
    public static void loadConfig() {
        Config config = Quakecraft.get().getCustomConfig().getConfigRequired("purchase-gui");
        Config lores = config.getConfigRequired("lore");
        buyingLores = lores.getMessageStrListRequired("buying");
        boughtLores = lores.getMessageStrListRequired("bought");
        selectedLores = lores.getMessageStrListRequired("selected");

        Config prefixes = config.getConfigRequired("name-prefix");
        prefixSelected = processColors(prefixes.getStringRequired("selected"));
        prefixSelectable = processColors(prefixes.getStringRequired("selectable"));
        prefixBuying = processColors(prefixes.getStringRequired("buying"));
        Quakecraft.get().getLogger().info("PurchaseGui's config loaded!");

        notEnoughMoney = config.getMessageRequired("not-enough-money");
    }

    private static String processColors(String selected) {
        return ChatColor.translateAlternateColorCodes('&', selected);
    }

    @SuppressWarnings("unchecked")
    public static PurchaseGui deserialize(Plugin plugin, String id, Config config, PurchaseManager manager) {
        try {
            PurchaseGui gui = new PurchaseGui(plugin, config);
            gui.add(
                    manager,
                    deserializeSlots(config.getCollectionRequired("slots"))
            );
            return gui;
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in gui " + id);
            throw e;
        }
    }

    public static int[] deserializeSlots(Collection<?> slots) {
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
