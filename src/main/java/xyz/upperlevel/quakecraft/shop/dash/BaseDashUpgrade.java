package xyz.upperlevel.quakecraft.shop.dash;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.CustomItem;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.util.Collections;

public class BaseDashUpgrade<P extends BaseDashUpgrade<P>> extends Purchase<P> {
    public static final PlaceholderValue<Short> GOT = PlaceholderValue.fake((short)DyeColor.GREEN.getWoolData());
    public static final PlaceholderValue<Short> MISSING = PlaceholderValue.fake((short)DyeColor.RED.getWoolData());

    private final CustomItem item;

    public BaseDashUpgrade(PurchaseManager<P> manager, String id, Config config) {
        super(manager, id, config);
        item = new CustomItem(new ItemStack(Material.STAINED_GLASS_PANE));
        item.setLore(config.getMessageStrList("lore", Collections.emptyList()));
    }

    @Override
    public CustomItem getIcon(QuakeAccount player) {
        item.setData(player.getPurchases().contains(this) ? GOT : MISSING);
        return item;
    }
}
