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
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.quakecraft.shop.event.PurchaseBuyEvent;
import xyz.upperlevel.quakecraft.shop.event.PurchaseSelectEvent;
import xyz.upperlevel.quakecraft.shop.railgun.Railgun;
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
import xyz.upperlevel.uppercore.itemstack.UItem;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;
import xyz.upperlevel.uppercore.placeholder.message.Message;
import xyz.upperlevel.uppercore.util.EnchantUtil;

import java.util.*;
import java.util.stream.Collectors;

import static xyz.upperlevel.quakecraft.Quake.getProfileController;

public class PurchaseGui extends ChestGui {
    public static List<PlaceholderValue<String>> buyingLores, boughtLores, selectedLores;
    public static String prefixSelected, prefixSelectable, prefixBuying;
    public static Message notEnoughMoney;
    public static PlaceholderValue<String> usedToMake, requireMissing, requireFound;

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
        super(config);
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

        for (PurchaseAdapter adapter : adapters) {
            Collection<SimplePurchase<?>> purchases = (Collection<SimplePurchase<?>>) adapter.manager.getPurchases().values();
            int[] slots = adapter.slots;
            int fillSize = purchases.size();

            if (purchases.size() > slots.length) {
                Quake.get().getLogger().severe("Cannot fill " + adapter.manager.getPurchaseName() + "'s inventory: too many items! found: " + purchases.size() + ", available slots: " + slots.length);
                fillSize = slots.length;
            }

            Iterator<SimplePurchase<?>> it = purchases.iterator();
            for (int i = 0; i < fillSize; i++) {
                purchaseMap.put(slots[i], it.next());
            }
        }
        dirty = false;
    }

    @Override
    public Inventory create(Player player) {
        if (dirty)
            reprint();
        Inventory inv = super.create(player);
        printPurchases(inv, player);
        return inv;
    }

    public void printPurchases(Inventory inv, Player player) {
        Profile profile = Quake.getProfileController().getOrCreateProfile(player);
        for (Map.Entry<Integer, Purchase<?>> p : purchaseMap.entrySet())
            inv.setItem(p.getKey(), getIcon(p.getValue(), profile, p.getValue().isSelected(profile)));
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
        Profile profile = Quake.getProfileController().getOrCreateProfile(player);
        Set<Purchase<?>> purchases = profile.getPurchases();
        if (!purchases.contains(purchase)) {
            //Require test
            if (purchase.getRequires().stream().anyMatch(r -> !r.test(profile)))
                return;

            if (purchase.getCost() > 0) {
                Balance b = EconomyManager.get(player);
                if (b == null) {
                    Quake.get().getLogger().severe("Economy not found!");
                    return;
                }
                if (b.has(purchase.getCost())) {
                    Quake.get().openConfirmPurchase(
                            player,
                            purchase,
                            ((Link) n -> onPurchaseSucceed(player, purchase)).and(GuiAction.back()),
                            GuiAction.back()
                    );
                } else {
                    notEnoughMoney.send(player, "required", String.valueOf(purchase.getCost()));
                }
            } else {
                onPurchaseSucceed(player, purchase);
                printPurchases(player.getOpenInventory().getTopInventory(), player);
            }
        } else { // The item was already purchased, reloads the selection.
            reloadSelection(player, slot, purchase);
        }
    }

    public void onPurchaseSucceed(Player player, Purchase<?> purchase) {
        PluginManager eventManager = Bukkit.getPluginManager();

        PurchaseBuyEvent buyEvent = new PurchaseBuyEvent(player, purchase);
        eventManager.callEvent(buyEvent);
        if (!buyEvent.isCancelled()) {
            Profile profile = Quake.getProfileController().getOrCreateProfile(player);

            // DB update:
            // T current purchases are retrieved, the new one is inserted among them,
            // and finally the whole list is sent back to the storage.
            // Could it has been done better? Definitely yes.
            Set<Purchase<?>> purchases = profile.getPurchases();
            purchases.add(purchase);
            getProfileController().updateProfile(
                    player.getUniqueId(),
                    new Profile().setPurchases(purchases)
            );

            PurchaseManager purchaseManager = purchase.getManager();
            PurchaseSelectEvent event = new PurchaseSelectEvent(player, purchaseManager.getSelected(profile), purchase);
            if (!event.isCancelled())
                purchaseManager.setSelected(profile.getPlayer(), event.getPurchase());
        }
    }

    protected ItemStack getIcon(Purchase<?> purchase, Profile profile, boolean selected) {
        UItem icon = purchase.getIcon(profile);
        Player p = profile.getPlayer();
        if (icon == null) {
            Quake.get().getLogger().severe("Null icon for purchase: \"" + purchase.getName());
            return null;
        }
        ItemStack item = icon.resolve(p);
        ItemMeta meta = item.getItemMeta();

        processMeta(profile, purchase, meta, selected);

        item.setItemMeta(meta);
        return item;
    }

    public void processMeta(Profile profile, Purchase<?> purchase, ItemMeta meta, boolean selected) {
        Player p = profile.getPlayer();
        boolean purchased = selected || purchase.getCost() == 0 || profile.getPurchases().contains(purchase);

        meta.setDisplayName(ChatColor.RESET + getPrefix(purchased, selected) + purchase.getName().resolve(p));

        if (selected && enchantSelected) {
            addGlow(meta);
        }

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
        metaLore.addAll(processRequires(purchase, profile));
        // Add "used to make %gun%" fields
        metaLore.addAll(processUsedToMake(purchase, profile));

        meta.setLore(metaLore);
    }

    public List<String> processRequires(Purchase<?> purchase, Profile profile) {
        List<String> lore = new ArrayList<>();
        List<Require> requires = purchase.getRequires();
        for (Require require : requires) {
            boolean pass = require.test(profile);

            PlaceholderValue<String> fmt = pass ? requireFound : requireMissing;

            String reqStr = fmt.resolve(profile.getPlayer(),
                    PlaceholderRegistry.create()
                            .set("require_name", require.getName(profile))
                            .set("require_type", require.getType())
            );
            String description = require.getDescription();
            if (description != null)// If there's no description nor progress the require is displayed as a one-line require
                lore.add("");
            lore.add(ChatColor.RESET + " " + reqStr);

            if (description != null) {
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

    public List<String> processUsedToMake(Purchase<?> purchase, Profile profile) {
        List<String> lore = new ArrayList<>();
        List<Railgun> makes = purchase.getUsedToMake();
        for (Railgun gun : makes) {
            PlaceholderRegistry reg = PlaceholderRegistry.create()
                    .set("gun_name", gun.getName())
                    .set("gun_id", gun.getId());

            lore.add(ChatColor.RESET + " " + usedToMake.resolve(profile.getPlayer(), reg));
        }
        return lore;
    }

    protected void addGlow(ItemMeta meta) {
        EnchantUtil.glow(meta);
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
    protected void reloadSelection(Player player, int slot, Purchase<?> sel) {
        PurchaseManager purchaseManager = sel.getManager();
        Purchase<?> old = purchaseManager.getSelected(Quake.getProfileController().getOrCreateProfile(player));
        if (old != sel) {
            PurchaseSelectEvent event = new PurchaseSelectEvent(player, old, sel);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                purchaseManager.setSelected(player, event.getPurchase());
                printPurchases(player.getOpenInventory().getTopInventory(), player);
            }
        }
    }

    public void addPurchase(PurchaseManager<?> manager, int[] slots) {
        adapters.add(new PurchaseAdapter(manager, slots));
    }

    @SuppressWarnings("unchecked")
    public static void loadConfig() {
        Config config = Quake.getConfigSection("purchase-gui");
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
        usedToMake = config.getMessageStr("used-to-make");
        requireMissing = config.getMessageStr("missing-requires");
        requireFound = config.getMessageStr("found-requires");
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
                    config.getRequired("slots", int[].class)
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
