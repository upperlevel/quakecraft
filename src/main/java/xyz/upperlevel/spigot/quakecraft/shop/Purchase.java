package xyz.upperlevel.spigot.quakecraft.shop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.upperlevel.uppercore.gui.config.itemstack.CustomItem;

@Getter
@RequiredArgsConstructor
public abstract class Purchase {
    private final String id;
    private final String name;
    private final float cost;
    private final CustomItem icon;
}
