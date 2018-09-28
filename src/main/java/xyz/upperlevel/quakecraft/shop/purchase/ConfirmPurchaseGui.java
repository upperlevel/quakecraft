package xyz.upperlevel.quakecraft.shop.purchase;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.ConfigConstructor;
import xyz.upperlevel.uppercore.config.ConfigProperty;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigException;
import xyz.upperlevel.uppercore.economy.Balance;
import xyz.upperlevel.uppercore.economy.EconomyManager;
import xyz.upperlevel.uppercore.gui.ChestGui;
import xyz.upperlevel.uppercore.gui.ConfigIcon;
import xyz.upperlevel.uppercore.gui.link.Link;
import xyz.upperlevel.uppercore.itemstack.CustomItem;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.io.File;
import java.util.Optional;

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
        setIcon(options.confirmSlots, ConfigIcon.of(confirmItem, this::onBuy));
        setIcon(options.cancelSlots, ConfigIcon.of(cancelItem, onCancel));

        placeholders.set("cost", purchase.getCostFormatted());
        placeholders.set("item_id", purchase.getId());
    }

    @Override
    public Inventory create(Player player) {
        String purchaseName = purchase.getName().resolve(player);

        placeholders.set("item_name", purchaseName);
        Inventory inv = super.create(player);

        CustomItem item = purchase.getIcon(Quakecraft.get().getPlayerManager().getPlayer(player));
        item.setDisplayName(PlaceholderValue.fake(purchaseName));

        inv.setItem(itemSlot, item.resolve(player));

        return inv;
    }

    @Override
    public String solveTitle(Player player) {
        return getTitle().resolve(player, placeholders);
    }

    public static Options load() {
        File file = new File(Quakecraft.get().getDataFolder(), "shop/confirm_gui.yml");
        return Config.fromYaml(file).get(Options.class, null);
    }

    public void onBuy(Player player) {
        Balance balance = EconomyManager.get(player);
        if(balance == null) {
            Quakecraft.get().getLogger().severe("No economy found!");
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

        @ConfigConstructor
        public Options(
                @ConfigProperty("title") PlaceholderValue<String> title,
                @ConfigProperty("size") Optional<Integer> size,
                @ConfigProperty("type") Optional<InventoryType> type,
                @ConfigProperty("item-slot") int itemSlot,
                @ConfigProperty("confirm.slots") int[] confirmSlots,
                @ConfigProperty("confirm.item") CustomItem confirmItem,
                @ConfigProperty("cancel.slots") int[] cancelSlots,
                @ConfigProperty("cancel.item") CustomItem cancelItem
        ) {
            if (!size.isPresent() && !type.isPresent()) {
                throw new InvalidConfigException("Both 'size' and 'type' aren't specified");
            }
            this.size = size.orElse(-1);
            this.type = type.orElse(null);
            this.itemSlot = itemSlot;
            this.confirmSlots = confirmSlots;
            this.confirmItem = confirmItem;
            this.cancelSlots = cancelSlots;
            this.cancelItem = cancelItem;
        }
    }
}
