package xyz.upperlevel.spigot.quakecraft.shop;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
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
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.util.*;
import java.util.stream.Collectors;

public class PurchasesGui<P extends Purchase<P>> extends ChestGui {
    private static List<PlaceholderValue<String>> buyingLores, boughtLores, selectedLores;
    private final int usableSlots[];
    private final PurchaseManager<P> purchaseManager;
    private Map<Integer, P> purchaseMap = new LinkedHashMap<>();
    @Getter
    private boolean dirty = true;
    @Getter
    @Setter
    private boolean enchantSelected = true;

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
            if (o instanceof Number)
                return ((Number) o).intValue();
            else
                throw new InvalidConfigurationException("Only numbers in the slots field!");
        }).toArray();
    }

    public void update() {
        this.purchaseMap.clear();
        int i = 0;
        Collection<P> purchases = purchaseManager.getPurchases().values();
        for (P p : purchases) {
            if (i >= usableSlots.length) {
                QuakeCraftReloaded.get().getLogger().severe("Cannot fill " + purchaseManager.getPurchaseName() + "'s inventory: too many items!");
                return;
            }
            int slot = usableSlots[i++];
            purchaseMap.put(slot, p);
        }
        dirty = false;
    }

    @Override
    public Inventory create(Player player) {
        if (dirty)
            update();
        Inventory inv = super.create(player);
        QuakePlayer qp = QuakePlayerManager.get().getPlayer(player);
        if(qp == null) {
            QuakeCraftReloaded.get().getLogger().severe("Player not registered in quake registry: " + player.getName());
            return inv;
        }
        for (Map.Entry<Integer, P> p : purchaseMap.entrySet())
            inv.setItem(p.getKey(), getIcon(p.getValue(), qp));
        return inv;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        P p = purchaseMap.get(event.getSlot());
        if (p != null)
            onClick((Player) event.getWhoClicked(), event.getSlot(), p);
        else
            super.onClick(event);
    }

    public void reloadSelection(QuakePlayer player, int slot, P purchase) {
        P oldPurchase = purchaseManager.getSelected(player);
        purchaseManager.setSelected(player, purchase);
        if (oldPurchase == purchase)
            return;
        Inventory inv = player.getPlayer().getOpenInventory().getTopInventory();
        for (Map.Entry<Integer, P> entry : purchaseMap.entrySet()) {
            if (entry.getValue() == oldPurchase) {
                inv.setItem(entry.getKey(), getIcon(oldPurchase, player));
            }
        }
        inv.setItem(slot, getIcon(purchase, player));
    }

    public void onClick(Player player, int slot, P purchase) {
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

    protected ItemStack getIcon(P purchase, QuakePlayer player) {
        CustomItem icon = purchase.getIcon();
        Player p = player.getPlayer();
        if (icon == null) {
            QuakeCraftReloaded.get().getLogger().severe("Null icon for purchase: \"" + purchase.getName());
            return null;
        }
        PlaceholderRegistry local = PlaceholderRegistry.create()
                .set("cost", purchase.getCost())
                .set("purchase", purchase.getId());
        ItemStack item = icon.resolve(p);
        ItemMeta meta = item.getItemMeta();
        P selected = purchaseManager.getSelected(player);
        meta.setDisplayName(purchase.getName().resolve(player.getPlayer()));
        // if the item wasn't enchanted with protection removes it
        if (!icon.getEnchantments().containsKey(Enchantment.PROTECTION_ENVIRONMENTAL))
            meta.removeEnchant(Enchantment.PROTECTION_ENVIRONMENTAL);
        // adds flow if selected and selects the right lore
        List<PlaceholderValue<String>> lores;
        if (selected == purchase) {
            lores = selectedLores;
            if(enchantSelected)
                EnchantGlow.addGlow(meta);
        } else if (player.getPurchases().contains(purchase))
            lores = boughtLores;
        else
            lores = buyingLores;
        List<String> metaLore = meta.getLore();
        if (metaLore == null)
            metaLore = new ArrayList<>();
        metaLore.addAll(
                lores.stream()
                        .map(lore -> lore.resolve(p, local))
                        .collect(Collectors.toList())
        );

        // Add requires
        List<String> requiresLores = new ArrayList<>();
        List<Require> requires = purchase.getRequires();
        for(Require require : requires) {
            String req = require.getRequires(player);
            boolean pass = require.test(player);
            String pre = pass ? Require.DONE : Require.MISSING;
            String description = require.description();
            if(description != null)//If there's no description nor progress the require is displayed as a one-line require
                requiresLores.add("");
            requiresLores.add(" " + pre + " " + req);

            if(description != null) {
                requiresLores.add("   " + description);
                if (!pass) {
                    String progress = require.getProgress();
                    if (progress != null)
                        requiresLores.add("   " + progress);
                }
            }
        }
        metaLore.addAll(requiresLores);


        meta.setLore(metaLore);
        item.setItemMeta(meta);
        return item;
    }

    public void setDirty() {
        dirty = true;
    }

    public static <P extends Purchase<P>> PurchasesGui<P> deserialize(Plugin plugin, String id, Config config, PurchaseManager<P> purchaseManager) {
        try {
            return new PurchasesGui<>(plugin, id, config, purchaseManager);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in gui " + id);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public static void load(Config config) {
        buyingLores = config.getMessageListRequired("buying");
        boughtLores = config.getMessageListRequired("bought");
        selectedLores = config.getMessageListRequired("selected");
        QuakeCraftReloaded.get().getLogger().info("PurchaseGui's config loaded!");
    }
}
