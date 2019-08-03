package xyz.upperlevel.quakecraft.shop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.gui.ChestGui;
import xyz.upperlevel.uppercore.gui.Gui;

@RequiredArgsConstructor
public abstract class Category {
    @Getter
    protected final PurchaseRegistry registry;
    @Getter
    protected Gui gui;

    protected void loadGui(String name, Config config) {
        gui = Quake.get().getGuis().register(name, config.get(ChestGui.class));
    }

    public void loadGui() {
        loadGui(getGuiRegistryName(), Quake.getConfigSection("shop." + getGuiLoc()));
    }

    public abstract String getGuiLoc();

    /**
     * @return The name associated with the Gui in the gui registry
     */
    public abstract String getGuiRegistryName();
}
