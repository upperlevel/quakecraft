package xyz.upperlevel.quakecraft.shop.dash;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.UItem;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.util.Collections;
import java.util.List;

public class BaseDashUpgrade<P extends BaseDashUpgrade<P>> extends Purchase<P> {
    public static final Material GOT = Material.GREEN_STAINED_GLASS_PANE;
    public static final Material MISSING = Material.RED_STAINED_GLASS_PANE;

    private final List<PlaceholderValue<String>> lore;

    public BaseDashUpgrade(PurchaseManager<P> manager, String id, Config config) {
        super(manager, id, config);
        lore = config.getMessageStrList("lore", Collections.emptyList());
    }

    @Override
    public UItem getIcon(QuakeAccount player) {
        Material mat = player.getPurchases().contains(this) ? GOT : MISSING;
        UItem item = new UItem(new ItemStack(mat));
        item.setLore(lore);

        return item;
    }
}
