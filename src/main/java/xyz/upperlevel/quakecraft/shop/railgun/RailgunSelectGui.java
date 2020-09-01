package xyz.upperlevel.quakecraft.shop.railgun;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.gui.ChestGui;
import xyz.upperlevel.uppercore.itemstack.UItem;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;
import xyz.upperlevel.uppercore.placeholder.message.Message;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static xyz.upperlevel.quakecraft.Quake.getProfile;
import static xyz.upperlevel.quakecraft.shop.purchase.PurchaseGui.getPrefix;

public class RailgunSelectGui extends ChestGui {
    private static Message GUN_ALREADY_SELECTED;
    private static Message GUN_SELECTED;
    private static Message GUN_PART_MISSING_HEADER;
    private static Message GUN_PART_MISSING_LINE;
    private static Message GUN_PART_MISSING_FOOTER;
    private List<PlaceholderValue<String>> commonLore, killMessageLore, selectedLore, selectableLore, missingPartsLore;

    private final RailgunManager manager;
    private Map<Integer, Railgun> gunMap = new LinkedHashMap<>();
    private int slots[];

    protected RailgunSelectGui(Config config, RailgunManager manager) {
        super(config);
        this.manager = manager;
        loadConfig(config);
    }


    public void print() {
        gunMap = new HashMap<>();

        Collection<Railgun> guns = manager.getRailguns();
        int i = 0;
        if(guns.size() > slots.length) {
            Quake.get().getLogger().severe("Cannot fill gun's inventory: too many guns!");
            return;
        }
        for (Railgun gun : guns) {
            gunMap.put(slots[i++], gun);
        }
    }

    @Override
    public Inventory create(Player player) {
        Inventory inv = super.create(player);
        Profile profile = Quake.getProfileController().getProfile(player);
        if(profile == null) {
            Quake.get().getLogger().severe("Player not registered in quake registry: " + player.getName());
            return inv;
        }
        printPurchases(inv, profile);
        return inv;
    }

    public void printPurchases(Inventory inv, Profile profile) {
        for (Map.Entry<Integer, Railgun> p : gunMap.entrySet())
            inv.setItem(p.getKey(), getIcon(p.getValue(), profile));
    }

    protected ItemStack getIcon(Railgun gun, Profile profile) {
        UItem icon = gun.getCase().getIcon();
        Player p = profile.getPlayer();

        ItemStack item = icon.resolve(p);
        ItemMeta meta = item.getItemMeta();

        processMeta(profile, gun, meta);

        item.setItemMeta(meta);
        return item;
    }

    public void processMeta(Profile profile, Railgun gun, ItemMeta meta) {
        Player p = profile.getPlayer();
        boolean selected = profile.getRailgun() == gun;
        boolean selectable = selected || gun.canSelect(profile);

        meta.setDisplayName(ChatColor.RESET + getPrefix(selectable, selected) + gun.getName().resolve(p));

        //Lore processing (additional lore + requires)
        List<String> metaLore = meta.getLore();
        if (metaLore == null)
            metaLore = new ArrayList<>();

        Stream<PlaceholderValue<String>> stream;
        if(gun.getKillMessage() != null)
            stream = killMessageLore.stream();
        else
             stream = commonLore.stream();

        metaLore.addAll(
                Stream.concat(
                        stream,
                        getLore(selectable, selected).stream()
                )
                .map(l -> ChatColor.RESET + l.resolve(p, gun.getPlaceholders()))
                .collect(Collectors.toList())
        );

        meta.setLore(metaLore);
    }

    protected List<PlaceholderValue<String>> getLore(boolean selectable, boolean selected) {
        if (selected)
            return selectedLore;
        else if (selectable)
            return selectableLore;
        else
            return missingPartsLore;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        Railgun p = gunMap.get(event.getSlot());
        if (p != null)
            onClick((Player) event.getWhoClicked(), event.getSlot(), p);
        else
            super.onClick(event);
    }

    public void onClick(Player player, int slot, Railgun gun) {
        Profile p = getProfile(player);
        if(p.getRailgun() == gun) {
            GUN_ALREADY_SELECTED.send(player);
            //You already have that gun equipped
        } else if(gun.canSelect(p)) {
            selectAndReload(p, slot, gun);
            GUN_SELECTED.send(player);
        } else {
            List<Purchase<?>> missingParts = gun.getComponents()
                    .stream()
                    .filter(s -> s.getCost() > 0)
                    .filter(o -> !p.getPurchases().contains(o))
                    .collect(Collectors.toList());

            GUN_PART_MISSING_HEADER.send(player, "parts", String.valueOf(missingParts.size()));
            for(Purchase<?> part : missingParts) {
                GUN_PART_MISSING_LINE.send(player, part.getPlaceholders());
            }
            GUN_PART_MISSING_FOOTER.send(player, "parts", String.valueOf(missingParts.size()));
        }
    }

    private void selectAndReload(Profile p, int slot, Railgun gun) {
        Railgun old = p.getRailgun();
        gun.select(p);

        //Find old gun's index
        int oldIndex = gunMap.entrySet()
                .stream()
                .filter(e -> e.getValue() == old)
                .mapToInt(Map.Entry::getKey)
                .findAny()
                .orElse(-1);

        Inventory inv = p.getPlayer().getOpenInventory().getTopInventory();
        inv.setItem(slot, getIcon(gun, p));
        if(oldIndex != -1)
            inv.setItem(oldIndex, getIcon(old, p));
    }

    protected void loadConfig(Config config) {
        Config lores = config.getConfigRequired("lores");
        commonLore = lores.getMessageRequired("common").getLines();
        killMessageLore = lores.getMessageRequired("kill-message").getLines();
        selectedLore = lores.getMessageRequired("selected").getLines();
        selectableLore = lores.getMessageRequired("selectable").getLines();
        missingPartsLore = lores.getMessageRequired("missing-parts").getLines();

        slots = config.getRequired("slots", int[].class);
    }

    public static void loadConfig() {
        Config cfg = Quake.getConfigSection("messages.shop.railgun");
        GUN_ALREADY_SELECTED = cfg.getMessageRequired("already-selected");
        GUN_SELECTED = cfg.getMessageRequired("selected");
        Config missing = cfg.getConfigRequired("parts-missing");
        GUN_PART_MISSING_HEADER = missing.getMessageRequired("header");
        GUN_PART_MISSING_LINE = missing.getMessageRequired("line");
        GUN_PART_MISSING_FOOTER = missing.getMessageRequired("footer");
    }
}
