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
import org.bukkit.plugin.PluginManager;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.quakecraft.QuakeAccountManager;
import xyz.upperlevel.quakecraft.shop.event.PurchaseBuyEvent;
import xyz.upperlevel.quakecraft.shop.event.PurchaseSelectEvent;
import xyz.upperlevel.quakecraft.shop.require.Require;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.ConfigConstructor;
import xyz.upperlevel.uppercore.config.ConfigProperty;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigException;
import xyz.upperlevel.uppercore.economy.Balance;
import xyz.upperlevel.uppercore.economy.EconomyManager;
import xyz.upperlevel.uppercore.gui.ChestGui;
import xyz.upperlevel.uppercore.gui.ConfigIcon;
import xyz.upperlevel.uppercore.gui.GuiAction;
import xyz.upperlevel.uppercore.gui.GuiSize;
import xyz.upperlevel.uppercore.gui.link.Link;
import xyz.upperlevel.uppercore.itemstack.CustomItem;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;
import xyz.upperlevel.uppercore.placeholder.message.Message;
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

    public PurchaseGui(Config config) {
        super(Quakecraft.get(), config);
    }

    @ConfigConstructor
    public PurchaseGui(
            @ConfigProperty("type") Optional<InventoryType> type,
            @ConfigProperty("size") Optional<GuiSize> size,
            @ConfigProperty("update-interval") Optional<Integer> updateInteval,
            @ConfigProperty("title") PlaceholderValue<String> title,
            @ConfigProperty(value = "icons", optional = true) List<ConfigIcon> icons
    ) {
        super(type, size, updateInteval, title, icons);
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
                Quake.get().getLogger().severe("Cannot fill " + adapter.manager.getPurchaseName() + "'s inventory: too many items!");
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
        QuakeAccount qp = QuakeAccountManager.get().getPlayer(player);
        if(qp == null) {
            Quake.get().getLogger().severe("Player not registered in quake registry: " + player.getName());
            return inv;
        }
        printPurchases(inv, qp);
        return inv;
    }

    public void printPurchases(Inventory inv, QuakeAccount player) {
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
        QuakeAccount p = QuakeAccountManager.get().getPlayer(player);
        Set<Purchase<?>> purchases = p.getPurchases();
        if (!purchases.contains(purchase)) {
            //Require test
            if(purchase.getRequires().stream().anyMatch(r -> !r.test(p)))
                return;

            if(purchase.getCost() > 0) {
                Balance b = EconomyManager.get(player);
                if (b == null) {
                    Quake.get().getLogger().severe("Economy not found!");
                    return;
                }
                if (b.has(purchase.getCost())) {
                    Quake.get().openConfirmPurchase(
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
    public void onPurchaseSucceed(QuakeAccount player, Purchase purchase) {
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

    protected ItemStack getIcon(Purchase<?> purchase, QuakeAccount player, boolean selected) {
        CustomItem icon = purchase.getIcon(player);
        Player p = player.getPlayer();
        if (icon == null) {
            Quake.get().getLogger().severe("Null icon for purchase: \"" + purchase.getName());
            return null;
        }
        ItemStack item = icon.resolve(p);
        ItemMeta meta = item.getItemMeta();

        processMeta(player, purchase, meta, selected);

        item.setItemMeta(meta);
        return item;
    }

    public void processMeta(QuakeAccount player, Purchase<?> purchase, ItemMeta meta, boolean selected) {
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

    public List<String> processRequires(Purchase<?> purchase, QuakeAccount player) {
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
    protected void reloadSelection(QuakeAccount player, int slot, Purchase<?> sel) {
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

    public void addPurchase(PurchaseManager<?> manager, int[] slots) {
        adapters.add(new PurchaseAdapter(manager, slots));
    }

    @SuppressWarnings("unchecked")
    public static void loadConfig() {
        Config config = Quake.get().getCustomConfig().getConfigRequired("purchase-gui");
        Config lores = config.getConfigRequired("lore");
        buyingLores = lores.getMessageStrListRequired("buying");
        boughtLores = lores.getMessageStrListRequired("bought");
        selectedLores = lores.getMessageStrListRequired("selected");

        Config prefixes = config.getConfigRequired("name-prefix");
        prefixSelected = processColors(prefixes.getStringRequired("selected"));
        prefixSelectable = processColors(prefixes.getStringRequired("selectable"));
        prefixBuying = processColors(prefixes.getStringRequired("buying"));
        Quake.get().getLogger().info("PurchaseGui's config loaded!");

        notEnoughMoney = config.getMessageRequired("not-enough-money");
    }

    private static String processColors(String selected) {
        return ChatColor.translateAlternateColorCodes('&', selected);
    }

    @SuppressWarnings("unchecked")
    public static PurchaseGui deserialize(String id, Config config, PurchaseManager manager) {
        try {
            PurchaseGui gui = new PurchaseGui(config);
            gui.addPurchase(
                    manager,
                    config.get("slots", int[].class, null)
            );
            return gui;
        } catch (InvalidConfigException e) {
            e.addLocation("in gui " + id);
            throw e;
        }
    }

    @AllArgsConstructor
    public static class PurchaseAdapter {
        private final PurchaseManager<?> manager;
        private final int[] slots;
    }
}
