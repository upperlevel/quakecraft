package xyz.upperlevel.spigot.quakecraft.shop.purchase;

import com.google.common.primitives.Ints;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;
import xyz.upperlevel.uppercore.economy.Balance;
import xyz.upperlevel.uppercore.economy.EconomyManager;
import xyz.upperlevel.uppercore.gui.ChestGui;
import xyz.upperlevel.uppercore.gui.Icon;
import xyz.upperlevel.uppercore.gui.link.Link;
import xyz.upperlevel.uppercore.itemstack.CustomItem;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.io.File;

public class ConfirmPurchaseGui extends ChestGui {
    private Purchase<?> purchase;
    private int itemSlot;
    private PlaceholderRegistry placeholders = PlaceholderRegistry.create();
    private final Link onAgree;

    public ConfirmPurchaseGui(Purchase<?> purchase, Options options, Link onAgree, Link onCancel) {
        super(options.size, options.type, options.title);
        this.purchase = purchase;
        itemSlot = options.itemSlot;

        CustomItem confirmItem = options.confirmItem.copy();
        confirmItem.setPlaceholders(placeholders);

        CustomItem cancelItem = options.cancelItem.copy();
        cancelItem.setPlaceholders(placeholders);

        this.onAgree = onAgree;
        setIcon(options.confirmSlots, Icon.of(confirmItem, this::onBuy));
        setIcon(options.cancelSlots, Icon.of(cancelItem, onCancel));

        placeholders.set("cost", purchase.getCostFormatted());
        placeholders.set("item_id", purchase.getId());
    }

    @Override
    public Inventory create(Player player) {
        String purchaseName = purchase.getName().resolve(player);

        placeholders.set("item_name", purchaseName);
        Inventory inv = super.create(player);

        CustomItem item = purchase.getIcon(QuakeCraftReloaded.get().getPlayerManager().getPlayer(player));
        item.setDisplayName(PlaceholderValue.fake(purchaseName));

        inv.setItem(itemSlot, item.resolve(player));

        return inv;
    }

    @Override
    public String solveTitle(Player player) {
        return getTitle().resolve(player, placeholders);
    }

    public static Options load() {
        File file = new File(QuakeCraftReloaded.get().getDataFolder(), "guis/confirm.yml");
        return new Options(Config.wrap(YamlConfiguration.loadConfiguration(file)));
    }

    public void onBuy(Player player) {
        Balance balance = EconomyManager.get(player);
        if(balance == null) {
            QuakeCraftReloaded.get().getLogger().severe("No economy found!");
            return;
        }
        if(balance.take(purchase.getCost()))
            onAgree.run(player);
        else
            PurchaseGui.notEnoughMoney.send(player);
    }


    public static class Options {
        private PlaceholderValue<String> title;
        private int size;
        private InventoryType type;
        private int itemSlot;
        private int[] confirmSlots;
        private CustomItem confirmItem;
        private int[] cancelSlots;
        private CustomItem cancelItem;

        public Options(Config config) {
            this.title = config.getMessageStrRequired("title");
            this.size = config.getInt("size", -1);
            if(size < 0) {
                type = config.getEnum("type", InventoryType.class);
                if(type == null)
                    throw new InvalidConfigurationException("Both 'size' and 'type' ar empty in Confirm GUI!");
            } else
                type = null;
            itemSlot = config.getIntRequired("item-slot");
            {
                Config confirm = config.getConfig("confirm");
                if(confirm.has("slot"))
                    confirmSlots = new int[]{confirm.getIntRequired("slot")};
                else if(confirm.has("slots"))
                    confirmSlots = Ints.toArray(confirm.getList("slots"));
                else
                    throw new InvalidConfigurationException("Both 'slot' and 'slots' are empty in confirm section of Confirm GUI!");
                confirmItem = confirm.getCustomItemRequired("item");
            }
            {
                Config cancel = config.getConfig("cancel");
                if(cancel.has("slot"))
                    cancelSlots = new int[]{cancel.getIntRequired("slot")};
                else if(cancel.has("slots"))
                    confirmSlots = Ints.toArray(cancel.getList("slots"));
                else
                    throw new InvalidConfigurationException("Both 'slot' and 'slots' are empty in cancel section of Confirm GUI!");
                cancelItem = cancel.getCustomItemRequired("item");
            }
        }
    }
}
